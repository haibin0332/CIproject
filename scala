          rdd to DF
          
          val sqlContext = new org.apache.spark.sql.SQLContext(sc)
          import sqlContext.implicits._
          val dataDF = trainDataSet.toDF()
          
          
val baseSchema =  new BaseSchema() 
initial 在初始化时被执行，构造函数的功能
class BaseSchema(sc: SparkContext,schemaFormatPath: String,delimiter:String)
{
  //Map[index,(name,type)]
  private val schemaFormatMap = mutable.Map[Int, (String, String)]()

  //Map[name,type]
  private val schema2FormatMap = mutable.Map[String, String]()

  //Array[name]
  private var schemaArray:Array[String] = Array[String]()

  //输入数据的schema信息
  private var schema: StructType = null

  //在主构造函数中调用初始化函数
  initial()

  def initial()
  {
    val tempSchemaFormat = sc.textFile(schemaFormatPath).collect().map(_.split(delimiter))

    var index = 0
    for(array <- tempSchemaFormat)
    {
      schemaFormatMap.put(index,(array.head,array.last))
      index += 1

      schema2FormatMap.put(array.head,array.last)

      schemaArray ++= array.head
    }

    schema = StructType(schemaArray.map(x => StructField(x, StringType, nullable=true)))
  }

  def getSchemaArray = schemaArray

  def getDataFrameSchema = schema

  def getSchema2FormatMap = schema2FormatMap
}

