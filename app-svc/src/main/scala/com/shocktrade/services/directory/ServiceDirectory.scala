package com.shocktrade.services.directory

import ServiceDirectory._
import scala.collection.concurrent.TrieMap
import scala.concurrent.{ ExecutionContext, Future, Promise }
import org.slf4j.LoggerFactory

import scala.language.postfixOps

/**
 * Service Directory
 * @author lawrence.daniels@gmail.com
 */
trait ServiceDirectory {
  private val domainMap = TrieMap[String, ServiceDomain[_]]()

  /**
   * Returns a dynamic view of the domain statistics
   */
  def domains: Seq[ServiceDomainStats] = (domainMap flatMap {
    case (name, d) => d.services map (svc =>
      ServiceDomainStats(name, svc.getClass.getSimpleName, svc.timesCalled, svc.avgResponseTime, svc.minResponseTime, svc.maxResponseTime))
  }).toSeq

  /**
   * Returns the specified service domain by name
   */
  def getDomain[S](domainName: String): Option[ServiceDomain[S]] = {
    domainMap.get(domainName) map( _.asInstanceOf[ServiceDomain[S]])
  }

  /**
   * Registers a service domain
   */
  def register[S](domainName: String, domainObj: ServiceDomain[S]) {
    domainMap += (domainName -> domainObj)
    ()
  }

  /**
   * Selection DSL - works just like SQL queries
   */
  def select[S](fields: String*) = new DSLFieldsStub[S](fields)

  /**
   * Selection DSL - Field section placeholder
   */
  class DSLFieldsStub[S](fields: Seq[String]) {

    def from(domainName: String): DSLDomainStub[S] = {
      domainMap.get(domainName) match {
        case Some(source) => new DSLDomainStub(fields.toSet, source.asInstanceOf[ServiceDomain[S]])
        case None => throw new IllegalArgumentException(s"Domain '$domainName' not found")
      }
    }

    def from(domain: ServiceDomain[S]): DSLDomainStub[S] = {
      new DSLDomainStub(fields.toSet, domain.asInstanceOf[ServiceDomain[S]])
    }
  }

  /**
   * Syntactic sugar for registering domands
   */
  implicit class DomainRegistrar(domainName: String) {

    def ~>[S](domain: ServiceDomain[S]) {
      domainMap += (domainName -> domain)
      ()
    }

    def ~>[S](sources: Seq[AssetDataSource[S]]) {
      val domain = SimpleServiceDomain[S]()
      sources foreach (src => domain.register(src))
      domainMap += (domainName -> domain)
      ()
    }
  }

}

/**
 * Service Directory Singleton
 * @author lawrence.daniels@gmail.com
 */
object ServiceDirectory {

  case class SimpleServiceDirectory() extends ServiceDirectory
  case class SimpleServiceDomain[T]() extends ServiceDomain[T]
  case class ServiceDomainStats(name: String, dataSource: String, timesCalled: Int, avgResponseTime: Double, minResponseTime: Long, maxResponseTime: Long)

  /**
   * The service directory provides a means for discovering the service (or services)
   * that are required to fulfill a data request
   * @author lawrence.daniels@gmail.com
   */
  trait ServiceDomain[T] {
    import collection.concurrent.TrieMap
    private lazy val logger = LoggerFactory.getLogger(getClass())
    private val searchPath = Seq(findSingleSatisfyingService _, findMultipleSatisfyingService _)
    private val dataSources = TrieMap[AssetDataSource[T], List[Long]]()

    def fetch(fields: Set[String], conditions: Seq[(String, Any)], partialResults: Boolean = true)(implicit ec: ExecutionContext, m: Manifest[T]): Future[Solution[T]] = {
      // create the bean instance
      val bean = m.runtimeClass.asInstanceOf[Class[T]].newInstance()

      // attempt to locate the qualified service(s)
      findService(fields, partialResults) match {
        case Some(svc) =>
          for {
            results <- svc.fetch(fields, conditions, bean)

            outcome = Solution[T](bean, results)
          } yield outcome
        case None =>
          val promise = Promise[Solution[T]]()
          promise.success(Solution[T](bean, Seq(NoServicesFound(fields))))
          promise.future
      }
    }

    /**
     * Registers a service domain
     */
    def register(dataSource: AssetDataSource[T]) {
      dataSources += (dataSource -> Nil)
      ()
    }

    def services: Seq[AssetDataSource[T]] = dataSources.keys.toSeq

    /**
     * Selection DSL - works just like SQL queries
     */
    def select(fields: String*) = new DSLDomainStub(fields.toSet, this)

    /**
     * Attempt to retrieve a service (or set of services) to fulfill the request
     * for the data associated to the given fields.
     * @param fields the given field definitions (e.g. "open", "lastTrade")
     * @return an option of a [[AssetDataSource data source]]
     */
    def findService(fields: Set[String], partialResults: Boolean = false): Option[AssetDataSource[T]] = {
      searchPath.foldLeft[Option[AssetDataSource[T]]](None) { (res, svc) => if (res.isEmpty) svc(fields, partialResults) else res }
    }

    private def findSingleSatisfyingService(fields: Set[String], partialResults: Boolean): Option[AssetDataSource[T]] = {
      (services sortBy (_.avgResponseTime)) find (svc => fields forall svc.provides.contains)
    }

    private def findMultipleSatisfyingService(fields: Set[String], partialResults: Boolean): Option[AssetDataSource[T]] = {

      def matchDataSources(prevFields: Set[String], fields: Set[String]): List[AssetDataSource[T]] = {
        if (fields.isEmpty) Nil
        else if (fields == prevFields) {
          val message = s"The following fields could not be resolved: ${fields mkString ", "}"
          logger.warn(message)
          if (!partialResults) throw new IllegalStateException(message)
          else List(UnmatchedFieldsDataSource(fields))
        } else {
          // generate a list of services and the field left unsatisfied
          val svcSeq = services map (svc => (svc, fields &~ (svc.provides & fields))) toSeq

          // find the service that satisfies the majority of the fields (fewest missing)
          val svc_? = svcSeq.sortBy(_._2.size).headOption
          svc_? match {
            case Some((svc, missing)) => svc :: matchDataSources(fields, missing)
            case None => logger.warn(s"No service found to fulfill these fields: $fields"); Nil
          }
        }
      }

      // find the list of services
      val svcSet = matchDataSources(Set.empty, fields).toSet
      if (svcSet.nonEmpty) Some(CompositeAssetDataSource(svcSet)) else None
    }
  }

  /**
   * Selection DSL - Domain selection placeholder
   */
  class DSLDomainStub[S](fields: Set[String], domain: ServiceDomain[S]) {

    def where(conditions: (String, Any)*)(implicit ec: ExecutionContext, m: Manifest[S]) = domain.fetch(fields, conditions)

  }

  /**
   * Represents a data source for asset (stock, ETF, etc.) information
   * @author lawrence.daniels@gmail.com
   */
  trait AssetDataSource[T] {
    private var queue = Seq[Long]()
    private var avgRespTime: Double = _
    private var minRespTime: Long = _
    private var maxRespTime: Long = _
    private var calls: Int = 0

    def fetch(fields: Set[String], where: Seq[(String, Any)], bean: T)(implicit ec: ExecutionContext): Future[Seq[LoadResult]] = {
      val outcome = load(fields, where, bean)
      outcome.onSuccess {
        case results =>
          // get the response times
          val responseTimes = results flatMap {
            case SuccessResult(_, _, responseTime) => Some(responseTime)
            case _ => None
          }

          // add them to the queue
          val newQueue = queue ++ responseTimes
          if (newQueue.length > 100) newQueue.drop(queue.length - 100)
          if (newQueue.nonEmpty) {
            avgRespTime = newQueue.sum / newQueue.size
            minRespTime = newQueue.min
            maxRespTime = newQueue.max
            calls += 1
          }
          queue = newQueue
      }
      outcome
    }

    def timesCalled = calls

    def avgResponseTime: Double = avgRespTime

    def minResponseTime: Long = minRespTime

    def maxResponseTime: Long = maxRespTime

    def load(fields: Set[String], where: Seq[(String, Any)], bean: T)(implicit ec: ExecutionContext): Future[Seq[LoadResult]]

    def provides: Set[String]

    protected def extractFields(beanClass: Class[_]): Set[String] = {
      beanClass.getDeclaredFields map (_.getName) toSet
    }

    protected def getCondition[T](name: String, conditions: Seq[(String, Any)]) = {
      val m = Map(conditions: _*)
      m.getOrElse(name, throw new IllegalArgumentException(s"'$name' is missing in where clause")).asInstanceOf[T]
    }

    override def toString = getClass.getSimpleName
  }

  /**
   * Represents a data source that returns a failed result for missing fields
   * @see AssetDataSource
   * @author lawrence.daniels@gmail.com
   */
  case class UnmatchedFieldsDataSource[T](missingFields: Set[String]) extends AssetDataSource[T] {
    override def provides = Set.empty

    override def load(fields: Set[String], where: Seq[(String, Any)], container: T)(implicit ec: ExecutionContext) = {
      val promise = Promise[Seq[LoadResult]]()
      promise.success(Seq(UnmatchedFields(missingFields)))
      promise.future
    }
  }

  /**
   * Represents a composite data source for asset information
   * @see AssetDataSource
   * @author lawrence.daniels@gmail.com
   */
  case class CompositeAssetDataSource[T](services: Set[AssetDataSource[T]]) extends AssetDataSource[T] {
    override def provides = (services flatMap (_.provides)).toSet

    override def load(fields: Set[String], where: Seq[(String, Any)], container: T)(implicit ec: ExecutionContext) = {
      // create a future of a sequence of operations
      val tasks = Future.sequence(services.toSeq map (_.fetch(fields, where, container)))

      for {
        // dereference the sequence of load results
        results <- tasks

        // flatten the results into a single sequence
        outcome = results flatten
      } yield outcome
    }
  }

  /**
   * Represents a solution (or result set) of a service query
   */
  case class Solution[T](bean: T, loadResults: Seq[LoadResult]) {

    def pctLoaded = {
      val values = loadResults map (r => if (r.isSuccess) 1.0 else 0.0)
      if (values.nonEmpty) values.sum / values.size else 0.0
    }
  }

  /**
   * Represents information about the outcome of a data loading operation
   * @author lawrence.daniels@gmail.com
   */
  sealed trait LoadResult { def isSuccess(): Boolean }

  /**
   * Represents information about the successful outcome of a data loading operation
   * @author lawrence.daniels@gmail.com
   */
  case class SuccessResult[T](dataSource: AssetDataSource[T], dataObject: Any, reponseTimeMillis: Long = 0) extends LoadResult {
    def as[S] = dataObject.asInstanceOf[S]
    def isSuccess = true
  }

  /**
   * Represents information about the failed outcome of a data loading operation
   * @author lawrence.daniels@gmail.com
   */
  case class UnmatchedFields(fields: Set[String]) extends LoadResult {
    def isSuccess = false
  }

  /**
   * Represents information about the failed outcome of a data loading operation
   * @author lawrence.daniels@gmail.com
   */
  case class NoServicesFound(fields: Set[String]) extends LoadResult {
    def isSuccess = false
  }

  /**
   * Provides a conversion from an option of a value to a Java equivalent value
   */
  def <~[S, T](option: => Option[T]): S = (option getOrElse null).asInstanceOf[S]

}