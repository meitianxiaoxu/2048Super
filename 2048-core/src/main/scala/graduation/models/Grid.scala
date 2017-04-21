package graduation.models

import argonaut._
import Argonaut._


/**
  * Created by KeXu on 2017/4/13.
  */
class Grid(val k:String, val p:Boolean,val s:Int,var d:Array[Array[Int]]) extends Serializable{

  var key:String=k
  var playerTurn:Boolean=p
  var data=d
  var step=s

  def getRow(rowIndex:Int):(Int,Int,Int,Int)={

    val row=data(rowIndex)
    (row(0),row(1),row(2),row(3))
  }

  def getCol(colIndex:Int):(Int,Int,Int,Int)={
    (data(0)(colIndex),data(1)(colIndex),data(2)(colIndex),data(3)(colIndex))
  }

  /**
    * 玩家切换
    * @return
    */
  def playerCutover():Boolean={
    playerTurn = !playerTurn
    playerTurn
  }

  // 朝某个方向移动后的局面,如果不能移动返回false
  def move(direct:Grid.Direct):Boolean = ???

  // 克隆该Grid对象
  override def clone(): Grid = ???

  // 评价该局面的评分
  def eval(): Double = ???

  // 返回空格的索引数组
  def availableCells():Array[(Int,Int)] = ???

  // 添加格子，设置value为0即为删除格子
  def setCell(index:(Int,Int) , value: Int):Grid = ???

  // 计算局面的平滑性
  def smoothness():Double = ???

  // 计算局面孤立点的数目
  def islands() = ???

  // 计算局面的单调性
  def monotonicity():Double= ???

  // 局面中最大的值
  def maxValue():Int= ???

}


object Grid extends Enumeration{

  type Direct = Value
  val UP = Value("up")
  val LEFT = Value("left")
  val DOWN = Value("down")
  val RIGHT = Value("right")
  val NONE = Value("none")
  val directs=List(UP,LEFT,DOWN,RIGHT)

  def apply(k: String,p: Boolean,s: Int,d: Array[Array[Int]]) = new Grid(k,p,s,d)

  implicit def GridEncodeJson: EncodeJson[Grid] =
    EncodeJson((g: Grid) =>
      ("key" := g.key) ->:
        ("playerTurn" := g.playerTurn) ->:
        ("data" := g.data) ->:
        jEmptyObject)

  implicit def GridDecodeJson: DecodeJson[Grid] =
    DecodeJson(g => for {
      key <- (g --\ "key").as[String]
      playerTurn <- (g --\ "playerTurn").as[Boolean]
      step <- (g --\ "step").as[Int]
      data <- (g --\ "data").as[Array[Array[Int]]]
    } yield new Grid(key,playerTurn,step,data))
}
