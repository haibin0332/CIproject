1) rdd to DF using Implicts   
 val sqlContext = new org.apache.spark.sql.SQLContext(sc)
 import sqlContext.implicits._
 val dataDF = trainDataSet.toDF()
          
2) 抽取类的思想          
val baseSchema =  new BaseSchema() 
initial 在初始化时被执行，构造函数的功能
class BaseSchema(sc: SparkContext,schemaFormatPath: String,delimiter:String)
{
  //Map[index,(name,type)]
  private val schemaFormatMap = mutable.Map[Int, (String, String)]()
  //Map[name,type]
  private val schema2FormatMap = mutable.Map[String, String]()
  //Array[name],保留特征索引顺序
  private var allSchema = ArrayBuffer[String]()
  //数值类型的schema列，保留特征索引顺序，排除UID
  private var numericSchema = ArrayBuffer[String]()
  //类别型的schema列，保留特征索引顺序，排除UID
  private var categorySchema = ArrayBuffer[String]()
  //输入数据的schema信息
  private var dataFrameSchema: StructType = null
  //Map[name,type]
  private var categorySchemaMap: mutable.Map[String, String] = null
  //主键ID
  private var uid: String = null
  //label用于标识预测值或者回归值所在列
  private var label: String = null
  //在主构造函数中调用初始化函数
  initial()
  def initial() {
    val tempSchemaFormat = sc.textFile(schemaFormatPath).collect().map(_.split(delimiter))
    var index = 0
    var name: String = null
    var dataType: String = null
    for (array <- tempSchemaFormat) {
      name = array.head.trim()
      dataType = array.last.trim()
      //默认第一列是UID，非UID才添加
      if (allSchema.nonEmpty) {
        if (DataType.CATEGORY.equals(dataType)) {
          categorySchema += name
        }
        else if (DataType.LABEL.equals(dataType)) {
          label = name
        } else {
          numericSchema += name
        }
      }
      allSchema += name
      schemaFormatMap.put(index, (name, dataType))
      index += 1

      schema2FormatMap.put(name, dataType)
    }

    dataFrameSchema = StructType(allSchema.map(x => StructField(x, StringType, nullable = true)))

    //UID应该从请求参数中获取
    uid = allSchema.head
  }

  def getSchemaArray = allSchema.toArray
  def getDataFrameSchema = dataFrameSchema
  def getSchema2FormatMap = schema2FormatMap
  def getUid = uid
  def getLabel = label
  def getCategorySchema = categorySchema.toArray
  def getCategorySchemaMap = {
    if (categorySchemaMap == null) {
      categorySchemaMap = schema2FormatMap.filter(x => x._2.equals(DataType.CATEGORY))
    }
    categorySchemaMap
  }

  /**
    * 当前只是派出了UID和类别型，加入新类型可能有问题
    *
    * @return 连续型schema列表
    */
  def getNumericSchema = {
    if (numericSchema.isEmpty) {
      numericSchema ++= getSchema2FormatMap.filter(x => {
        !x._2.equals(DataType.CATEGORY)
      }).filter(x => {
        !x._1.equals(uid)
      }).filter(x => {
        !x._1.equals(label)
      }).keySet
    }
    numericSchema.toArray
  }
}

3) 模板的输出方式
outputToFile(processOutputPath, "stringindexer", "stringindexer", sc.parallelize(postStringIndexer._2.toList).coalesce(1, false))
  def outputToFile[T](processOutputPath:String, fileName:String, info:String, content: => RDD[T])={
    val outputFilePath = processOutputPath.concat(fileName)
    if (outputFilePath != null && outputFilePath != "") {
      //输出文件路径开头不是以"hdfs"开头的，向非hdfs系统输出(eg. 本地文件系统)
      if (!outputFilePath.startsWith("hdfs")) {
        logger.info(s"Please input hdfs format")
      } else {
        val outPath = new Path(outputFilePath)
        val hdfs = outPath.getFileSystem(new Configuration())
        if (hdfs.exists(outPath))
          hdfs.delete(outPath, true) //删除输出目录
        content.saveAsTextFile(outputFilePath)
      }
      logger.info(s"Output $info data to path [$outputFilePath] complete")
    }
  }
