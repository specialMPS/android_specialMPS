package com.example.specialmps

import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.PercentFormatter
import kotlinx.android.synthetic.main.activity_result.*
import kotlin.random.Random

class ResultActivity : AppCompatActivity() {

    var final_depression:Int=0 //우울감 최종 점수
    var final_angry:Int=0 //분노 최종 점수
    var final_neutrality:Int=0 //중립 최종 점수
    var final_happiness:Int=0 //행복 최종 점수
    lateinit var userid : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        if(intent.hasExtra("userID")){
            userid = intent.getStringExtra("userID").toString()
        }

        //최종 결과 데이터 가져오고, 세부 감정은 arraylist에 집어넣기
        //final_depression=
        //final_angry=
        //final_neutrality=
        //final_happiness=

        val score=findViewById<TextView>(R.id.resultfigure)
        score.text="슬픔 $final_depression%"

        val pie=findViewById<PieChart>(R.id.pie_chart)
        create_pie_chart(pie)

        val comment1=findViewById<TextView>(R.id.comment_result)
        comment1.text="$userid 님의 최종 점수는 우울 $final_depression%, 불안 $final_angry%, 기쁨 $final_happiness%, 중립 $final_neutrality% 입니다."

        val comment2=findViewById<TextView>(R.id.comment_chat)
        comment2.text=make_comment(final_depression)

        val graph=findViewById<LineChart>(R.id.line_chart)



    }

    fun make_comment(depression:Int):String{
        var last_comment=""
        val num=(0..1).random()+1
        when(depression){
            0 ->{
                val resID:Int=resources.getIdentifier("zero${num}","string",this.packageName)
                last_comment=resources.getString(resID)
            }
            in 1..9 -> {
                val resID:Int=resources.getIdentifier("zero${num}","string",this.packageName)
                last_comment=resources.getString(resID)
            }
            in 10..19 -> {
                val resID:Int=resources.getIdentifier("ten${num}","string",this.packageName)
                last_comment=resources.getString(resID)
            }
            in 20..29 -> {
                val resID:Int=resources.getIdentifier("twenty${num}","string",this.packageName)
                last_comment=resources.getString(resID)
            }
            in 30..39 -> {
                val resID:Int=resources.getIdentifier("thirty${num}","string",this.packageName)
                last_comment=resources.getString(resID)
            }
            in 40..49 -> {
                val resID:Int=resources.getIdentifier("forty${num}","string",this.packageName)
                last_comment=resources.getString(resID)
            }
            in 50..59 -> {
                val resID:Int=resources.getIdentifier("fifty${num}","string",this.packageName)
                last_comment=resources.getString(resID)
            }
            in 60..69 -> {
                val resID:Int=resources.getIdentifier("sixty${num}","string",this.packageName)
                last_comment=resources.getString(resID)
            }
            in 70..79 -> {
                val resID:Int=resources.getIdentifier("seventy${num}","string",this.packageName)
                last_comment=resources.getString(resID)
            }
            in 80..89 ->{
                val resID:Int=resources.getIdentifier("eighty${num}","string",this.packageName)
                last_comment=resources.getString(resID)
            }
            in 90..100 ->{
                val resID:Int=resources.getIdentifier("ninety${num}","string",this.packageName)
                last_comment=resources.getString(resID)
            }
        }

        return last_comment
    }

    fun create_pie_chart(pieChart:PieChart){
        //감정분석 세부 결과 가져와서 원형 차트에 집어넣기

        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled=false

        pieChart.isDrawHoleEnabled=true
        pieChart.setHoleColor(Color.WHITE)
        pieChart.holeRadius=55f

        pieChart.isHighlightPerTapEnabled=true
        pieChart.legend.isEnabled=false
        pieChart.setEntryLabelColor(Color.WHITE)
        pieChart.setEntryLabelTextSize(12f)

        //sample data
        val emotions=arrayListOf<PieEntry>()
        emotions.add(PieEntry(15f,"화남"))
        emotions.add(PieEntry(10f,"괴로움"))
        emotions.add(PieEntry(15f,"우울함"))
        emotions.add(PieEntry(10f,"중립"))
        emotions.add(PieEntry(10f,"기쁨"))
        emotions.add(PieEntry(10f,"긴장됨"))
        emotions.add(PieEntry(10f,"비참함"))
        emotions.add(PieEntry(10f,"놀람"))
        emotions.add(PieEntry(10f,"피로함"))

        val dataSet=PieDataSet(emotions,"Result Emotions")
        dataSet.setDrawIcons(false)
        dataSet.sliceSpace=1f

        //sample color
        val colors= arrayListOf<Int>()
        colors.add(Color.BLUE)
        colors.add(Color.YELLOW)
        colors.add(Color.RED)
        colors.add(Color.BLACK)
        colors.add(Color.CYAN)
        colors.add(Color.DKGRAY)
        colors.add(Color.GREEN)
        colors.add(Color.LTGRAY)
        colors.add(Color.MAGENTA)

        dataSet.colors=colors

        val data= PieData(dataSet)
        data.setValueFormatter(PercentFormatter())
        data.setValueTextSize(15f)
        data.setValueTypeface(Typeface.DEFAULT_BOLD)
        data.setValueTextColor(Color.WHITE)
        pieChart.data=data

        pieChart.highlightValues(null)
        pieChart.invalidate()
    }

    fun create_line_chart(lineChart:LineChart){
        //최근 10개의 슬픔 퍼센티지 가지고 와서 그래프화로 추이 나타내기

        lineChart.extraBottomOffset=5f
        lineChart.description.isEnabled=false

        val legend=lineChart.legend //차트 범례
        legend.verticalAlignment=Legend.LegendVerticalAlignment.BOTTOM
        legend.horizontalAlignment=Legend.LegendHorizontalAlignment.CENTER
        legend.form=Legend.LegendForm.CIRCLE
        legend.formSize=10f
        legend.textSize=13f
        legend.textColor=Color.parseColor("#ccd5ae")
        legend.orientation=Legend.LegendOrientation.VERTICAL
        legend.setDrawInside(false)
        legend.yEntrySpace=5f
        legend.isWordWrapEnabled=true
        legend.xOffset=80f
        legend.yOffset=20f
        legend.calculatedLineSizes

        val xAxis=lineChart.xAxis //x축
        xAxis.setDrawAxisLine(false)
        xAxis.setDrawAxisLine(false)
        xAxis.position=XAxis.XAxisPosition.BOTTOM
        xAxis.granularity=1f
        xAxis.textSize=14f
        xAxis.textColor=Color.parseColor("#d4a373")
        xAxis.spaceMax=0.1f
        xAxis.spaceMin=0.1f

        val yAxisLeft=lineChart.axisLeft //왼쪽 y축, 선 유무, 데이터 최솟값,최댓값, 색상
        yAxisLeft.textSize=14f
        yAxisLeft.textColor=Color.parseColor("#d4a373")
        yAxisLeft.setDrawAxisLine(true)
        yAxisLeft.axisLineWidth=2f
        yAxisLeft.axisMaximum=100f
        yAxisLeft.axisMinimum=0f
        //yAxisLeft.granularity

        val yAxisRight=lineChart.axisRight //오른쪽 y축
        yAxisRight.textSize=14f
        yAxisRight.textColor=Color.parseColor("#d4a373")
        yAxisRight.setDrawAxisLine(false)
        yAxisRight.setDrawLabels(false)
        yAxisRight.axisLineWidth=2f
        yAxisRight.axisMaximum=100f
        yAxisRight.axisMinimum=0f
        //yAxisRight.granularity

        val entry= arrayListOf<Entry>()
        //데이터베이스에서 최근 10개 날짜, 우울 최종 수치 가져오기


        val data=LineDataSet(entry)
        val chartData=LineData()




        lineChart.data
        lineChart.invalidate()
    }
}