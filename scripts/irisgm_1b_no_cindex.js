print("start: " + new Date());
for (var i = 1; i <= 1000000000; i++) {
  var numbervalue = i % 1000;
  var datevalue = "2014-02-25 12:00:00";
  db.gm_std_measurements.insert({ "fkDataSeriesId" : numbervalue, "measDateUtc" : datevalue, "measDateSite" : datevalue, "project_id" : numbervalue, "measvalue" : numbervalue, "refMeas" : false, "reliability" : 1 });
}
print("end: " + new Date());