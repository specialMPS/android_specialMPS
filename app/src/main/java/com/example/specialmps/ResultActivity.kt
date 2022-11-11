package com.example.specialmps

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class ResultActivity : AppCompatActivity() {

    var final_depression:Float=0f //우울감 최종 점수
    var final_angry:Float=0f //분노 최종 점수
    var final_neutrality:Float=0f //중립 최종 점수
    var final_happiness:Float=0f //행복 최종 점수
    val mDatabase=FirebaseDatabase.getInstance()
    lateinit var userid : String
    lateinit var name : String
    lateinit var emotionScore:EmotionInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        if(intent.hasExtra("userID")){
            userid = intent.getStringExtra("userID").toString()
        }

        if(intent.hasExtra("name")){
            name = intent.getStringExtra("name").toString()
        }

        if(intent.hasExtra("emotionScore")){
            emotionScore = intent.getSerializableExtra("emotionScore") as EmotionInfo
        }

        //뒤로가기 버튼
        val toolbar: Toolbar =findViewById(R.id.toolbar_channel)
        setSupportActionBar(toolbar)
        //supportActionBar!!.setDisplayHomeAsUpEnabled(true) //뒤로가는 버튼 생성
        supportActionBar!!.setDisplayShowTitleEnabled(false) //toolbar에 title 보이지 않도록 설정


        //최종 결과 데이터 가져오고, 세부 감정은 arraylist에 집어넣기
        final_depression=49.2f
        final_angry=42.5f
        final_neutrality=3.1f
        final_happiness=5.2f

//        val score=findViewById<TextView>(R.id.resultfigure)
//        score.text="슬픔 $final_depression%"

        val pie=findViewById<PieChart>(R.id.pie_chart)
        create_pie_chart(pie)

        val comment1=findViewById<TextView>(R.id.comment_result)
        comment1.text="$name 님의 최종 점수는 우울 $final_depression%, 분노 $final_angry%, 행복 $final_happiness%, 중립 $final_neutrality% 입니다."
        val comment2=findViewById<TextView>(R.id.comment_chat)
        comment2.text=make_comment(final_depression)

        val graph=findViewById<LineChart>(R.id.line_chart)
        create_line_chart(graph)


    }

    fun make_comment(depression:Float):String{
        var last_comment=""
        val num=(0..1).random()+1
        when(depression){
            0f ->{
                val resID:Int=resources.getIdentifier("zero${num}","string",this.packageName)
                last_comment=resources.getString(resID)
            }
            in 1f..9f -> {
                val resID:Int=resources.getIdentifier("zero${num}","string",this.packageName)
                last_comment=resources.getString(resID)
            }
            in 10f..19f -> {
                val resID:Int=resources.getIdentifier("ten${num}","string",this.packageName)
                last_comment=resources.getString(resID)
            }
            in 20f..29f -> {
                val resID:Int=resources.getIdentifier("twenty${num}","string",this.packageName)
                last_comment=resources.getString(resID)
            }
            in 30f..39f -> {
                val resID:Int=resources.getIdentifier("thirty${num}","string",this.packageName)
                last_comment=resources.getString(resID)
            }
            in 40f..49f -> {
                val resID:Int=resources.getIdentifier("forty${num}","string",this.packageName)
                last_comment=resources.getString(resID)
            }
            in 50f..59f -> {
                val resID:Int=resources.getIdentifier("fifty${num}","string",this.packageName)
                last_comment=resources.getString(resID)
            }
            in 60f..69f -> {
                val resID:Int=resources.getIdentifier("sixty${num}","string",this.packageName)
                last_comment=resources.getString(resID)
            }
            in 70f..79f -> {
                val resID:Int=resources.getIdentifier("seventy${num}","string",this.packageName)
                last_comment=resources.getString(resID)
            }
            in 80f..89f ->{
                val resID:Int=resources.getIdentifier("eighty${num}","string",this.packageName)
                last_comment=resources.getString(resID)
            }
            in 90f..100f ->{
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
        pieChart.setDrawCenterText(true)

        pieChart.isHighlightPerTapEnabled=true
        pieChart.legend.isEnabled=false
        pieChart.setEntryLabelColor(Color.WHITE)
        pieChart.setEntryLabelTextSize(12f)

        //sample data
        pieChart.centerText="우울 $final_depression%"
        pieChart.setCenterTextSize(30f)
        pieChart.setCenterTextColor(Color.parseColor("#d4a373"))
        val emotions=arrayListOf<PieEntry>()
        emotions.add(PieEntry(10.5f,"화남"))
        emotions.add(PieEntry(19.6f,"괴로움"))
        emotions.add(PieEntry(24.3f,"우울함"))
        emotions.add(PieEntry(3.1f,"중립"))
        emotions.add(PieEntry(5.2f,"기쁨"))
        emotions.add(PieEntry(9.3f,"긴장됨"))
        emotions.add(PieEntry(11.6f,"비참함"))
        emotions.add(PieEntry(3.1f,"놀람"))
        emotions.add(PieEntry(13.3f,"피로함"))

        val dataSet=PieDataSet(emotions,"Result Emotions")
        dataSet.setDrawIcons(false)
        dataSet.sliceSpace=1f

        //sample color
        val colors= arrayListOf<Int>()
        colors.add(Color.parseColor("#333d29"))
        colors.add(Color.parseColor("#414833"))
        colors.add(Color.parseColor("#656d4a"))
        colors.add(Color.parseColor("#a4ac86"))
        colors.add(Color.parseColor("#b6ad90"))
        colors.add(Color.parseColor("#a68a64"))
        colors.add(Color.parseColor("#936639"))
        colors.add(Color.parseColor("#7f4f24"))
        colors.add(Color.parseColor("#582f0e"))

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

//    private fun generateCenterSpannableText(): SpannableString {
//        val imageSpan=ImageSpan(this,R.drawable.maincharacter)
//        val spannableString=SpannableString(" ")
//        spannableString.setSpan(imageSpan," ".length-1,"".length,0)
//        return spannableString
//    }

    fun create_line_chart(lineChart:LineChart){
        //최근 10개의 슬픔 퍼센티지 가지고 와서 그래프화로 추이 나타내기

        lineChart.clear()

        val entry= arrayListOf<Entry>()
        val chartData=LineData()

        //sample data
        val cal=Calendar.getInstance()
        cal.time=Date()
        val df=SimpleDateFormat("yyyy-MM-dd")
        var datetime=1f
        entry.add(Entry(1f,0f))
        entry.add(Entry(2f,0f))
        entry.add(Entry(3f,56.3f))
        entry.add(Entry(4f,34.2f))
        entry.add(Entry(5f,19.8f))
        entry.add(Entry(6f,7.9f))
        entry.add(Entry(7f,90.1f))
        entry.add(Entry(8f,64.4f))
        entry.add(Entry(9f,15.6f))
        entry.add(Entry(10f,49.2f))

        val lineDatas=LineDataSet(entry, "최근 10개 우울감정 수치")
        chartData.addDataSet(lineDatas)

        lineDatas.lineWidth=3f
        lineDatas.circleRadius=6f
        lineDatas.setDrawValues(true)
        lineDatas.setDrawCircleHole(true)
        lineDatas.setDrawCircles(true)
        lineDatas.setDrawHorizontalHighlightIndicator(false)
        lineDatas.setDrawHighlightIndicators(false)
        lineDatas.setColor(Color.parseColor("#c2c5aa"))
        lineDatas.setCircleColor(Color.parseColor("#c2c5aa"))
        lineDatas.valueTextSize=10f

        //데이터베이스에서 최근 10개 날짜, 우울 최종 수치 가져오기
        val result=mDatabase.getReference("Result").child(userid).limitToLast(10)
            .addChildEventListener(object: ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    Log.i("snapshot.key : ",snapshot.key.toString())

                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    TODO("Not yet implemented")
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    TODO("Not yet implemented")
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    TODO("Not yet implemented")
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

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
//        xAxis.valueFormatter=object:IndexAxisValueFormatter(){
//            override fun getFormattedValue(value: Float): String {
//
//            }
//        }
        xAxis.setDrawAxisLine(false)
        xAxis.setDrawGridLines(false)
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
        yAxisRight.axisLineWidth=1f
        yAxisRight.axisMaximum=100f
        yAxisRight.axisMinimum=0f
        //yAxisRight.granularity

        lineChart.data=chartData
        lineChart.invalidate()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.toolbar_exit -> {
                //Log.i("toolbar exit btn"," click")
                //종료 버튼을 눌렀을 때 다이얼로그 생성
                val builder= AlertDialog.Builder(this)
                builder.setMessage("결과 보기가 종료됩니다.")
                    .setPositiveButton("확인",object : DialogInterface.OnClickListener{
                        override fun onClick(p0: DialogInterface?, p1: Int) {
                            //메인 페이지로 넘어가기
                            changeActivitytoMain()
                        }
                    }).setNegativeButton("취소",null).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chatting_tool,menu)
        return super.onCreateOptionsMenu(menu)
    }

    fun changeActivitytoMain(){
        val i= Intent(this, MenuActivity::class.java)
        i.putExtra("userID",userid)
        startActivity(i)
        finish()
    }
}

class TimeAxisValueFormat:IndexAxisValueFormatter(){
    //x축을 날짜 형식으로 포맷
    override fun getFormattedValue(value: Float): String {
        return super.getFormattedValue(value)
    }
}