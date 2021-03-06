package shapeless.contrib.scalaz

import scalaz._

import shapeless._
import shapeless.contrib._

private trait Empty {

  def emptyProduct = new Monoid[HNil] with Order[HNil] with Show[HNil] {
    def zero = HNil
    def append(f1: HNil, f2: => HNil) = HNil
    override def equal(a1: HNil, a2: HNil) = true
    def order(x: HNil, y: HNil) = Monoid[Ordering].zero
    override def shows(f: HNil) = "HNil"
  }

}

// Products

private trait ProductSemigroup[F, T <: HList]
  extends Semigroup[F :: T]
  with Product[Semigroup, F, T] {

  def append(f1: λ, f2: => λ) =
    F.append(f1.head, f2.head) :: T.append(f1.tail, f2.tail)

}

private trait ProductMonoid[F, T <: HList]
  extends ProductSemigroup[F, T]
  with Monoid[F :: T]
  with Product[Monoid, F, T] {

  def zero = F.zero :: T.zero

}

private trait ProductEqual[F, T <: HList]
  extends Equal[F :: T]
  with Product[Equal, F, T] {

  def equal(a1: λ, a2: λ) =
    F.equal(a1.head, a2.head) && T.equal(a1.tail, a2.tail)

}

private trait ProductOrder[F, T <: HList]
  extends ProductEqual[F, T]
  with Order[F :: T]
  with Product[Order, F, T] {

  override def equal(a1: λ, a2: λ) =
    super[ProductEqual].equal(a1, a2)

  def order(x: λ, y: λ) =
    Semigroup[Ordering].append(F.order(x.head, y.head), T.order(x.tail, y.tail))

}

private trait ProductShow[F, T <: HList]
  extends Show[F :: T]
  with Product[Show, F, T] {

  override def shows(f: λ) =
    F.shows(f.head) ++ " :: " ++ T.shows(f.tail)

  override def show(f: λ) =
    F.show(f.head) ++ Cord(" :: ") ++ T.show(f.tail)

}


// Isos

private trait IsomorphicSemigroup[A, B]
  extends Semigroup[A]
  with Isomorphic[Semigroup, A, B] {

  def append(f1: A, f2: => A) =
    iso.from(B.append(iso.to(f1), iso.to(f2)))

}

private trait IsomorphicMonoid[A, B]
  extends IsomorphicSemigroup[A, B]
  with Monoid[A]
  with Isomorphic[Monoid, A, B] {

  def zero = iso.from(B.zero)

}

private trait IsomorphicEqual[A, B]
  extends Equal[A]
  with Isomorphic[Equal, A, B] {

  override def equal(a1: A, a2: A) =
    B.equal(iso.to(a1), iso.to(a2))

}

private trait IsomorphicOrder[A, B]
  extends IsomorphicEqual[A, B]
  with Order[A]
  with Isomorphic[Order, A, B] {

  override def equal(a1: A, a2: A) =
    super[IsomorphicEqual].equal(a1, a2)

  def order(x: A, y: A) =
    B.order(iso.to(x), iso.to(y))

}

private trait IsomorphicShow[A, B]
  extends Show[A]
  with Isomorphic[Show, A, B] {

  override def shows(f: A) =
    B.shows(iso.to(f))

  override def show(f: A) =
    B.show(iso.to(f))

}

trait Instances {

  // Instances

  implicit def SemigroupI: TypeClass[Semigroup] = new TypeClass[Semigroup] with Empty {
    def product[F, T <: HList](f: Semigroup[F], t: Semigroup[T]) =
      new ProductSemigroup[F, T] { def F = f; def T = t }
    def derive[A, B](b: Semigroup[B], ab: Iso[A, B]) =
      new IsomorphicSemigroup[A, B] { def B = b; def iso = ab }
  }

  implicit def MonoidI: TypeClass[Monoid] = new TypeClass[Monoid] with Empty {
    def product[F, T <: HList](f: Monoid[F], t: Monoid[T]) =
      new ProductMonoid[F, T] { def F = f; def T = t }
    def derive[A, B](b: Monoid[B], ab: Iso[A, B]) =
      new IsomorphicMonoid[A, B] { def B = b; def iso = ab }
  }

  implicit def EqualI: TypeClass[Equal] = new TypeClass[Equal] with Empty {
    def product[F, T <: HList](f: Equal[F], t: Equal[T]) =
      new ProductEqual[F, T] { def F = f; def T = t }
    def derive[A, B](b: Equal[B], ab: Iso[A, B]) =
      new IsomorphicEqual[A, B] { def B = b; def iso = ab }
  }

  implicit def ShowI: TypeClass[Show] = new TypeClass[Show] with Empty {
    def product[F, T <: HList](f: Show[F], t: Show[T]) =
      new ProductShow[F, T] { def F = f; def T = t }
    def derive[A, B](b: Show[B], ab: Iso[A, B]) =
      new IsomorphicShow[A, B] { def B = b; def iso = ab }
  }

  implicit def OrderI: TypeClass[Order] = new TypeClass[Order] with Empty {
    def product[F, T <: HList](f: Order[F], t: Order[T]) =
      new ProductOrder[F, T] { def F = f; def T = t }
    def derive[A, B](b: Order[B], ab: Iso[A, B]) =
      new IsomorphicOrder[A, B] { def B = b; def iso = ab }
  }


  // Boilerplate

  implicit def deriveSemigroup[F, G <: HList](implicit iso: Iso[F, G], hlistInst: TypeClass.HListInstance[Semigroup, G]): Semigroup[F] =
    TypeClass.deriveFromIso[Semigroup, F, G]

  implicit def deriveMonoid[F, G <: HList](implicit iso: Iso[F, G], hlistInst: TypeClass.HListInstance[Monoid, G]): Monoid[F] =
    TypeClass.deriveFromIso[Monoid, F, G]

  implicit def deriveEqual[F, G <: HList](implicit iso: Iso[F, G], hlistInst: TypeClass.HListInstance[Equal, G]): Equal[F] =
    TypeClass.deriveFromIso[Equal, F, G]

  implicit def deriveShow[F, G <: HList](implicit iso: Iso[F, G], hlistInst: TypeClass.HListInstance[Show, G]): Show[F] =
    TypeClass.deriveFromIso[Show, F, G]

  implicit def deriveOrder[F, G <: HList](implicit iso: Iso[F, G], hlistInst: TypeClass.HListInstance[Order, G]): Order[F] =
    TypeClass.deriveFromIso[Order, F, G]

}

// vim: expandtab:ts=2:sw=2
