package blitz

typealias Provider<T> = () -> T

typealias Operator<I, O> = (I) -> O