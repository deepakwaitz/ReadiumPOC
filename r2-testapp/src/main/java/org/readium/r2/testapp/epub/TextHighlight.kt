package org.readium.r2.testapp.epub

interface TextHighlight {
    fun playHighlightTextChanged(text: String, start: Int, end: Int) {}
}