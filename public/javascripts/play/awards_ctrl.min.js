/**
 * Awards Controller
 * @author lawrence.daniels@gmail.com
 */
angular
	.module('shocktrade')
	.controller('AwardsCtrl', ['$scope', '$http', 'MySession', function($scope, $http, MySession) {	
	
  $scope.awardImageMap = [];
  $scope.getAwardImage = function(code) {
	  return $scope.awardImageMap[code] || '';
  };
  	
  $scope.setupAwards = function() {
	// create a mapping of the user's awards		
	var myAwards = { };
	var userAwards = MySession.userProfile.awards || [];
	for(var n = 0; n < userAwards.length; n++) {
		myAwards[userAwards[n]] = true;
	}
	
	// setup the ownership for the awards
	var imageMap = [];
	var awards = $scope.awards;
	for(var n = 0; n < awards.length; n++) {
		// set the ownership indicator
		awards[n].owned = myAwards[awards[n].code] || false;
		
		// create the code to image mapping
		imageMap[awards[n].code] = awards[n].icon;
	}
	
	$scope.awardImageMap = imageMap;
  };
	  
  $scope.awards = [
   	{ "name" : "Told your friends!", "code" : "FACEBOOK", "icon" : "/assets/images/awards/facebook.jpg", "description" : "Posted to FaceBook from ShockTrade" },
   	{ "name" : "Right back at cha!", "code" : "FBLIKEUS", "icon" : "/assets/images/awards/facebook.jpg", "description" : "Told your friends and \"Liked\" ShockTrade on FaceBook (Pays 1 Perk)" },
   	{ "name" : "A Little bird told me...", "code" : "TWITTER", "icon" : "/assets/images/awards/twitter.png", "description" : "Posted a Tweet from ShockTrade" },
   	{ "name" : "Your colleagues had to know!", "code" : "LINKEDIN", "icon" : "/assets/images/awards/linkedin.png", "description" : "Posted to LinkedIn from ShockTrade" },
   	{ "name" : "Told your followers!", "code" : "GOOGPLUS", "icon" : "/assets/images/awards/google_plus.jpg", "description" : "Posted to Google+ from ShockTrade" },
   	{ "name" : "A Picture is worth a thousand words!", "code" : "INSTGRAM", "icon" : "/assets/images/awards/instagram.png", "description" : "Posted to Instagram from ShockTrade" },
   	{ "name" : "Self-promotion pays!", "code" : "MEPROMO", "icon" : "/assets/images/awards/instagram.png", "description" : "Posted to FaceBook, Google+, Instagram, LinkedIn and Twitter from ShockTrade (Pays 1 Perk)" },
   	{ "name" : "The Ultimate Socialite!", "code" : "SOCLITE", "icon" : "/assets/images/awards/instagram.png", "description" : "Earned all social awards" },
   	{ "name" : "Perks of the Job!", "code" : "PERK", "icon" : "/assets/images/awards/perk.gif", "description" : "Earned a Perk" },
   	{ "name" : "It's time for the Perk-u-lator!", "code" : "5PERKS", "icon" : "/assets/images/awards/perk.gif", "description" : "Earned 5 Perks" },
   	{ "name" : "Perk Master!", "code" : "10PERKS", "icon" : "/assets/images/awards/perk.gif", "description" : "Earned 10 Perks" },
   	{ "name" : "Euro-Tactular!", "code" : "EUROTACT", "icon" : "/assets/images/awards/euro-tactular.png", "description" : "Traded the Euro" },
   	{ "name" : "International Shopper", "code" : "INTNSHPR", "icon" : "/assets/images/awards/international_shopper.gif", "description" : "Traded three or more currencies" },
   	{ "name" : "Pay Dirt!", "code" : "PAYDIRT", "icon" : "/assets/images/awards/pay_dirt.png", "description" : "Your portfolio gained 100% or more" },
   	{ "name" : "Mad Money!", "code" : "MADMONEY", "icon" : "/assets/images/awards/made_money.png", "description" : "Your portfolio gained 250% or more" },
   	{ "name" : "Crystal Ball", "code" : "CRYSTBAL", "icon" : "/assets/images/awards/crystal_ball.png", "description" : "Your portfolio gained 500% or more" },
   	{ "name" : "Checkered Flag", "code" : "CHKDFLAG", "icon" : "/assets/images/awards/checkered_flag.png", "description" : "Finished a Game!" },
   	{ "name" : "Gold Trophy", "code" : "GLDTRPHY", "icon" : "/assets/images/awards/gold_trophy.png", "description" : "Came in first place! (out of 14 players)" }];
     	
	$scope.owned = function(award) {
		var awards = MySession.userProfile ? MySession.userProfile.awards : [];
		if(awards) {
			for(var n = 0; n < awards.length; n++) {
				if( awards[n] == award.code ) return true;
			}
		}
		return false;
	};
	
	// watch for changes to the player's profile
	$scope.$watch("MySession.userProfile", function() {
		$scope.setupAwards();
	});
	
}]);