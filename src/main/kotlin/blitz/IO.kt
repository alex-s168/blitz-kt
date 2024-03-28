package blitz

@Deprecated(
    level = DeprecationLevel.ERROR,
    message = "Will be removed in the future!",
    replaceWith = ReplaceWith(
        "Terminal.warn",
        "blitz.term.Terminal"
    )
)
fun warn(msg: String) {
    System.err.println(msg)
}