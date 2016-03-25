create column family TradingHistory
with comparator = UTF8Type
and default_validation_class = UTF8Type
and column_metadata = [
		 {column_name : id, validation_class : UTF8Type}
		 {column_name : symbol, validation_class : UTF8Type}
         {column_name : exchange, validation_class : UTF8Type}
         {column_name : tradeDate, validation_class : UTF8Type}
         {column_name : prevClose, validation_class : UTF8Type}
         {column_name : open, validation_class : UTF8Type}
         {column_name : close, validation_class : UTF8Type}
         {column_name : chg, validation_class : UTF8Type}
         {column_name : changePct, validation_class : UTF8Type}
         {column_name : high, validation_class : UTF8Type}
         {column_name : low, validation_class : UTF8Type}
         {column_name : spread, validation_class : UTF8Type}
         {column_name : volume, validation_class : UTF8Type}
];
