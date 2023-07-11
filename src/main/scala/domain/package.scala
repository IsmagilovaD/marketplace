import cats.data.ReaderT
import cats.effect.IO
package object domain {
  type IOWithRequestContext[A] = ReaderT[IO, RequestContext, A]
}
