package com.soywiz.korge.intellij.util

import korlibs.io.async.*

fun <T> Signal<T>.addCallInit2(initial: T, handler: (T) -> Unit): AutoCloseable {
	handler(initial)
	return add(handler)
}

fun Signal<Unit>.addCallInit2(handler: (Unit) -> Unit): AutoCloseable = addCallInit2(Unit, handler)
