package model

case class SummaryDetails(totalBuilds:Integer, buildsSucess:Integer, buildsFails:Integer,lastWeekTotal:Integer,
    lastWeekSucess:Integer, lastWeekFails:Integer,totalLastMonth:Integer, lastMonthSucess:Integer, lastMonthFails:Integer,
    userSucess:String,userFailure:String)
    
object Summary {

}