package com.rakra.wordsprint.data.math

/**
 * Returns the tens and above digits of the number rounded **up** to the nearest multiple of ten.
 *
 * For example:
 * - 23 rounds up to 30, so returns 3
 * - 20 rounds up to 20, so returns 2
 * - 49 rounds up to 50, so returns 5
 *
 * @param number The integer number to round and extract the tens digit from.
 * @return The tens digit of the rounded-up number.
 *
 * This is useful for figuring out how many practices we want for each unit in the progression database
 */
fun getRoundedUpTens(number: Int): Int {
    if (number % 10 == 0) {
        return number / 10
    }
    return (number / 10 + 1)
}
