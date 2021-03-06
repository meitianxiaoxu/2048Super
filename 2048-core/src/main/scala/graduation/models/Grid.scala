package graduation.models

import argonaut.Argonaut._
import argonaut._

import scala.collection.mutable.ArrayBuffer
import scala.util.control.Breaks._

/**
  * Created by KeXu on 2017/4/13.
  */
class Grid(val k: String, val p: Boolean, val s: Int, var d: Array[Array[Int]]) extends Serializable {

  var key: String = k
  var playerTurn: Boolean = p
  var data = d
  var step = s

  def getRow(rowIndex: Int): (Int, Int, Int, Int) = {

    val row = data(rowIndex)
    (row(0), row(1), row(2), row(3))
  }

  def getCol(colIndex: Int): (Int, Int, Int, Int) = {
    (data(0)(colIndex), data(1)(colIndex), data(2)(colIndex), data(3)(colIndex))
  }

  /**
    * 玩家切换
    *
    * @return
    */
  def playerCutover(): Boolean = {
    playerTurn = !playerTurn
    playerTurn
  }

  // 朝某个方向移动后的局面,如果不能移动返回false
  def move(direct: Grid.Direct): Boolean = {
    direct match {
      //move up
      case Grid.UP =>
        val baseArray = data.map(_.clone()).clone()
        for (y <- 0 to 3) {
          var x = 0
          while (x < 4) {
            breakable {
              for (x1 <- (x + 1) to 3) {
                if (data(x1)(y) > 0) {
                  if (data(x)(y) <= 0) {
                    setCell((x, y), data(x1)(y))
                    setCell((x1, y), 0)
                    x = x - 1
                  } else if (data(x)(y) == (data(x1)(y))) {
                    setCell((x, y), data(x1)(y) * 2)
                    setCell((x1, y), 0)
                  }
                  break()
                }
              }
            }
            x = x + 1
          }
        }
        !equalData(baseArray, data)
      //move down
      case Grid.DOWN =>
        val baseArray = data.map(_.clone()).clone()
        for (y <- 0 to 3) {
          var x = 3
          while (x >= 0) {
            breakable {
              var x1 = x - 1
              while (x1 >= 0) {
                if (data(x1)(y) > 0) {
                  if (data(x)(y) <= 0) {
                    setCell((x, y), data(x1)(y))
                    setCell((x1, y), 0)
                    x = x + 1
                  } else if (data(x)(y) == data(x1)(y)) {
                    setCell((x, y), data(x1)(y) * 2)
                    setCell((x1, y), 0)
                  }
                  break()
                }
                x1 = x1 - 1
              }
            }
            x = x - 1
          }
        }
        !equalData(baseArray, data)
      //move left
      case Grid.LEFT =>
        val baseArray = data.map(_.clone()).clone()
        for (x <- 0 to 3) {
          var y = 0
          while (y < 4) {
            breakable {
              for (y1 <- (y + 1) to 3) {
                if (data(x)(y1) > 0) {
                  if (data(x)(y) <= 0) {
                    setCell((x, y), data(x)(y1))
                    setCell((x, y1), 0)
                    y = y - 1
                  } else if (data(x)(y) == (data(x)(y1))) {
                    setCell((x, y), data(x)(y1) * 2)
                    setCell((x, y1), 0)
                  }
                  break()
                }
              }
            }
            y = y + 1
          }
        }
        !equalData(baseArray, data)
      //move right
      case Grid.RIGHT =>
        val baseArray = data.map(_.clone()).clone()
        for (x <- 0 to 3) {
          var y = 3
          while (y >= 0) {
            breakable {
              var y1 = y - 1
              while (y1 >= 0) {
                if (data(x)(y1) > 0) {
                  if (data(x)(y) <= 0) {
                    setCell((x, y), data(x)(y1))
                    setCell((x, y1), 0)
                    y = y + 1
                  } else if (data(x)(y) == data(x)(y1)) {
                    setCell((x, y), data(x)(y1) * 2)
                    setCell((x, y1), 0)
                  }
                  break;
                }
                y1 = y1 - 1
              }
            }
            y = y - 1
          }
        }
        !equalData(baseArray, data)
    }

  }

  // 克隆该Grid对象
  override def clone(): Grid = {
    val copydata = data.map(_.clone()).clone()
    new Grid(key, playerTurn, step, copydata)
  }


  // 返回空格的索引数组
  def availableCells(): Array[(Int, Int)] = {
    val noneArray = new ArrayBuffer[(Int, Int)]()
    for (row <- 0 to 3) {
      for (col <- 0 to 3) {
        if (data(row)(col).equals(0)) {
          noneArray += ((row, col))
        }
      }
    }
    noneArray.toArray
  }

  // 添加格子，设置value为0即为删除格子
  def setCell(index: (Int, Int), value: Int): Grid = {
    data(index._1)(index._2) = value
    this
  }

  def equalData(data1: Array[Array[Int]], data2: Array[Array[Int]]): Boolean = {
    for (row <- 0 to 3; col <- 0 to 3) {
      if (data1(row)(col) != data2(row)(col)) {
        return false
      }
    }
    true
  }

  // 计算局面的平滑性
  def smoothness(): Double = {
    var smoothness = 0.0
    for (x <- 0 to 3; y <- 0 to 3) {
      if (data(x)(y) != 0) {
        val value = math.log(data(x)(y)) / math.log(2)
        for (d <- List(Grid.RIGHT, Grid.DOWN)) {
          val vec = Grid.vectors.get(d).get
          val tc = findFarthestPosition((x, y), vec)._2
          if (withinBounds(tc) && data(tc._1)(tc._2) != 0) {
            val tv = math.log(data(tc._1)(tc._2)) / math.log(2)
            smoothness -= math.abs(value - tv)

          }
        }
      }
    }
    smoothness
  }

  // 通过模型评分
  def modelScore():Double={
    var scoreArray=new Array[Int](24)
    var (penalty,add)=(0,0)
    //var (score1,score2,score3,penalty)=(0.0,0.0,0.0,0.0)
    for (x <- 0 to 3; y <- 0 to 3) {
      val value=data(x)(y)
      if(value!=0){
        add+=value
        // model1 score
        scoreArray(0)+=(value*Grid.model1(x)(y))
        scoreArray(1)+=(value*Grid.model1(x)(3-y))

        scoreArray(2)+=(value*Grid.model1(y)(x))
        scoreArray(3)+=(value*Grid.model1(3-y)(x))

        scoreArray(4)+=(value*Grid.model1(3-x)(3-y))
        scoreArray(5)+=(value*Grid.model1(3-x)(y))

        scoreArray(6)+=(value*Grid.model1(y)(3-x))
        scoreArray(7)+=(value*Grid.model1(3-y)(3-x))

        // model2 score
        scoreArray(8)+=(value*Grid.model2(x)(y))
        scoreArray(9)+=(value*Grid.model2(x)(3-y))

        scoreArray(10)+=(value*Grid.model2(y)(x))
        scoreArray(11)+=(value*Grid.model2(3-y)(x))

        scoreArray(12)+=(value*Grid.model2(3-x)(3-y))
        scoreArray(13)+=(value*Grid.model2(3-x)(y))

        scoreArray(14)+=(value*Grid.model2(y)(3-x))
        scoreArray(15)+=(value*Grid.model2(3-y)(3-x))

        //// model3 score
        scoreArray(16)+=(value*Grid.model3(x)(y))
        scoreArray(17)+=(value*Grid.model3(x)(3-y))

        scoreArray(18)+=(value*Grid.model3(y)(x))
        scoreArray(19)+=(value*Grid.model3(3-y)(x))

        scoreArray(20)+=(value*Grid.model3(3-x)(3-y))
        scoreArray(21)+=(value*Grid.model3(3-x)(y))

        scoreArray(22)+=(value*Grid.model3(y)(3-x))
        scoreArray(23)+=(value*Grid.model3(3-y)(3-x))

        Grid.directs.foreach(d=>{
          val v=Grid.vectors(d)
          val p=(x+v._1,y+v._2)
          if(withinBounds(p)){
            penalty+=math.abs(data(p._1)(p._2)-data(x)(y))
          }
        })

      }
    }
//    if(maxValue()>=2048) {
//      val loop = new Breaks
//      loop.breakable {
//        Grid.directs.foreach(d => {
//          val newGrid = this.clone()
//          if (newGrid.move(d)) {
//            val (m1, m2) = newGrid.max2Value()
//            penalty = m1 + m2 - newGrid.data(0)(0) - newGrid.data(0)(1)
//            if (penalty == 0) {
//              loop.break
//            }
//          }
//        })
//      }
//    }
    if(maxValue()>=4096){
      penalty -= add * 2
    }
    scoreArray.max-penalty
  }

  // 计算局面单调性
  def monotonicity(): Double = {
    var result = Array(0.0, 0.0, 0.0, 0.0)
    // 上下方向
    List(0, 1, 2, 3).foreach({ x =>
      var current = 0
      var next = current + 1
      while (next < 4) {
        while (next < 4 && data(x)(next) == 0) {
          next += 1
        }
        if (next >= 4) {
          next -= 1
        }
        val currentValue = if (data(x)(current) != 0) {
          math.log(data(x)(current)) / math.log(2)
        } else {
          0
        }
        val nextValue = if (data(x)(next) != 0) {
          math.log(data(x)(next)) / math.log(2)
        } else {
          0
        }
        if (currentValue > nextValue) {
          result(0) += nextValue - currentValue
        } else if (nextValue > currentValue) {
          result(1) += currentValue - nextValue
        }
        current = next
        next += 1
      }
    })

    // 左右方向
    List(0, 1, 2, 3).foreach({ y =>
      var current = 0
      var next = current + 1
      while (next < 4) {
        while (next < 4 && data(next)(y) == 0) {
          next += 1
        }
        if (next >= 4) {
          next -= 1
        }
        val currentValue = if (data(current)(y) != 0) {
          math.log(data(current)(y)) / math.log(2)
        } else {
          0
        }
        val nextValue = if (data(next)(y) != 0) {
          math.log(data(next)(y)) / math.log(2)
        } else {
          0
        }
        if (currentValue > nextValue) {
          result(2) += nextValue - currentValue
        } else if (nextValue > currentValue) {
          result(3) += currentValue - nextValue
        }
        current = next
        next += 1
      }
    })
    math.max(result(0), result(1)) + math.max(result(2), result(3))
  }

  // 计算局面孤立点的数目
  def islands(): Double = {
    var dataMark = (for (x <- 0 to 3; y <- 0 to 3) yield ((x, y) -> false)) toMap

    def mark(x: Int, y: Int, value: Int): Unit = {
      if (x >= 0 && x <= 3 && y >= 0 && y <= 3 && data(x)(y) != 0 && data(x)(y) == value && !dataMark((x, y))) {
        dataMark += ((x, y) -> true)
        Grid.directs.foreach(d => {
          val vec = Grid.vectors(d)
          mark(x + vec._1, y + vec._2, value)
        })
      }
    }

    var isLands = 0;
    for (x <- 0 to 3; y <- 0 to 3) {
      if (data(x)(y) != 0 && !dataMark((x, y))) {
        isLands += 1
        mark(x, y, data(x)(y))
      }
    }

    isLands
  }

  // 寻找局面最远的格子
  def findFarthestPosition(c: (Int, Int), vec: (Int, Int)): ((Int, Int), (Int, Int)) = {
    var previous: (Int, Int) = null
    var cell: (Int, Int) = c
    do {
      previous = cell
      cell = (cell._1 + vec._1, cell._2 + vec._2)
    } while (withinBounds(cell) && data(cell._1)(cell._2) == 0)
    (previous, cell)
  }

  // 检查cell是否在边界内
  def withinBounds(cell: (Int, Int)): Boolean = {
    cell._1 >= 0 && cell._1 <= 3 && cell._2 >= 0 && cell._2 <= 3
  }


  // 局面中最大的值
  def maxValue(): Int = {
    val mValue = data.maxBy(_.max).max
    mValue
  }

  // 局面中最大的值
  def max2Value(): (Int,Int) = {
    var (m1,m2)=(0,0)
    for (x <- 0 to 3; y <- 0 to 3) {
      val v=data(x)(y)
      if(v>m1){
        m1=v
      }else if(v>m2){
        m2=v
      }
    }
    (m1,m2)
  }

  override def toString: String = {
    val str = new StringBuilder
    data.foreach { row =>
      str.append(row.mkString(" ") + ",")
    }
    "key:" + key + " step:" + step + " data:" + str.toString()
  }
}


object Grid extends Enumeration {

  type Direct = Value
  val UP = Value(0,"UP")
  val LEFT = Value(3,"LEFT")
  val DOWN = Value(2,"DOWN")
  val RIGHT = Value(1,"RIGHT")
  val NONE = Value(4,"NONE")
  val directs = List(UP, LEFT, DOWN, RIGHT)

  val vectors = Map[Direct, (Int, Int)](
    UP -> (0, -1),
    RIGHT -> (1, 0),
    DOWN -> (0, 1),
    LEFT -> (-1, 0)
  )

//  val model1=Array(
//    Array(6,5,4,1),
//    Array(5,4,1,0),
//    Array(4,1,0,-1),
//    Array(1,0,-1,-2)
//  )

  val model1=Array(
    Array(16,15,14,13),
    Array(9,10,11,12),
    Array(8,7,6,5),
    Array(1,2,3,4)
  )

  val model2=Array(
    Array(16,15,12,4),
    Array(14,13,11,3),
    Array(10,9,8,2),
    Array(7,6,5,1)
  )

  val model3=Array(
    Array(16,15,14,4),
    Array(13,12,11,3),
    Array(10,9,8,2),
    Array(7,6,5,1)
  )

//  val model3=Array(
//      Array(16,15,14,4),
//      Array(13,12,11,3),
//      Array(10,9,8,2),
//      Array(7,6,5,1)
//    )

  def apply(k: String, p: Boolean, s: Int, d: Array[Array[Int]]) = new Grid(k, p, s, d)

  implicit def GridDecodeJson: DecodeJson[Grid] =
    DecodeJson(g => for {
      key <- (g --\ "key").as[String]
      step <- (g --\ "step").as[Int]
      data <- (g --\ "data").as[Array[Array[Int]]]
    } yield new Grid(key, true, step, data))
}
