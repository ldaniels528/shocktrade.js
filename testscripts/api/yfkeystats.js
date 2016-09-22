(function () {

    const htmlToJson = require("html-to-json");

    const promise = htmlToJson.request('https://finance.yahoo.com/quote/AAPL/key-statistics', {
        'text': function ($doc) {
            return $doc.find('script').text();
        }
    }, function (err, result) {
        //console.log(JSON.stringify(JSON.parse(result), null, '/t'));
        console.log(result);
    });

    promise.done(function (result) {
        //Works as well
    });

})();
