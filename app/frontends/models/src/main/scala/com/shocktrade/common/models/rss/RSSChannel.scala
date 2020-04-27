package com.shocktrade.common.models.rss

import scala.scalajs.js

/**
 * RSS Channel
 * @example {{{
 * feedUrl: 'https://www.reddit.com/.rss'
 * title: 'reddit: the front page of the internet'
 * description: ""
 * link: 'https://www.reddit.com/'
 * items:
 *     - title: 'The water is too deep, so he improvises'
 *       link: 'https://www.reddit.com/r/funny/comments/3skxqc/the_water_is_too_deep_so_he_improvises/'
 *       pubDate: 'Thu, 12 Nov 2015 21:16:39 +0000'
 *       creator: "John Doe"
 *       content: '<a href="http://example.com">this is a link</a> &amp; <b>this is bold text</b>'
 *       contentSnippet: 'this is a link & this is bold text'
 *       guid: 'https://www.reddit.com/r/funny/comments/3skxqc/the_water_is_too_deep_so_he_improvises/'
 *       categories:
 *           - funny
 *       isoDate: '2015-11-12T21:16:39.000Z'
 * }}}
 */
@js.native
trait RSSChannel extends js.Object {

  def items: js.Array[RSSFeed] = js.native

  def image: RSSImage = js.native

  def title: js.UndefOr[String] = js.native

  def description: js.UndefOr[String] = js.native

  def generator: js.UndefOr[String] = js.native

  def link: js.UndefOr[String] = js.native

  def language: js.UndefOr[String] = js.native

  def copyright: js.UndefOr[String] = js.native

  def lastBuildDate: js.UndefOr[String] = js.native

  def ttl: js.UndefOr[String] = js.native

}

@js.native
trait RSSFeed extends js.Object {

  def title: js.UndefOr[String] = js.native

  def link: js.UndefOr[String] = js.native

  def pubDate: js.UndefOr[String] = js.native

  //def creator: js.UndefOr[String] = js.native

  def content: js.UndefOr[String] = js.native

  def contentSnippet: js.UndefOr[String] = js.native

  def guid: js.UndefOr[String] = js.native

  //def categories: js.UndefOr[js.Array[String]] = js.native

  def isoDate: js.UndefOr[String] = js.native

}

@js.native
trait RSSImage extends js.Object {

  def link: js.UndefOr[String] = js.native

  def url: js.UndefOr[String] = js.native

  def title: js.UndefOr[String] = js.native

}