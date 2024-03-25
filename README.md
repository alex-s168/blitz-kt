# Blitz
Kotlin library which mainly focuses on functional programming.

Features:
- Monads
- Either
- Caching delegated property (similar to lazy)
- A lot of sequence utils
- Streaming IO using kotlinx.io
- ByteVec (alternative to ByteBuf)
- BitVec (similar to bit sets in other languages)
- "Lazy" sequences
- A lot of generative sequence types
- BitField

## How to get
```kotlin
repositories {
    maven {
        name = "alex's repo"
        url = uri("http://207.180.202.42:8080/libs")
        isAllowInsecureProtocol = true
    }
}

dependencies {
    implementation("me.alex_s168:blitz:0.1")
}
```