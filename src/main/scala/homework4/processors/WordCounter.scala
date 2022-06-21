package homework4.processors

import homework4.Processor
import homework4.http.HttpResponse
import homework4.math.Monoid

import concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future
import scala.language.postfixOps

case class WordCount(wordToCount: Map[String, Int]):
  def print = wordToCount.map((s: String, i: Int) => println(s + ": " + i))

object WordCount:

  given Monoid[WordCount] = new Monoid[WordCount]:
    override def identity: WordCount = new WordCount(Map[String, Int]())
    extension (a: WordCount)
      def |+|(b: WordCount): WordCount =
        var newWordCount = Map[String, Int]()

        for i <- a.wordToCount.keySet ++ b.wordToCount.keySet do
          newWordCount = newWordCount + (i -> (a.wordToCount.getOrElse(i, 0).asInstanceOf[Int] + b.wordToCount
            .getOrElse(i, 0)
            .asInstanceOf[Int]))
        return new WordCount(newWordCount)

def wordsOf(text: String): List[String] = text.split("\\W+").toList.filter(_.nonEmpty)

object WordCounter extends Processor[WordCount]:
  def apply(url: String, response: HttpResponse): Future[WordCount] = Future {
    wordsOf(response.body).foldRight(new WordCount(Map[String, Int]()))((x: String, y: WordCount) =>
      new WordCount(Map(x -> 1)) |+| y
    )
  }
