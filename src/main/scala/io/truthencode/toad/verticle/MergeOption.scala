package io.truthencode.toad.verticle

/**
  * Simple enumeration used to determine add vs replace
  */
object MergeOption extends Enumeration {
type MergeOption = Value
 val MERGE,REPLACE = Value
}

