package main

import day1.FuelCalculator
import day2.ProgramAlarm
import java.io.File

fun main() {
    println("day 1")
    FuelCalculator.readFileInput(File("src/main/resources/day1/input.txt")).let {
        println("result: ${FuelCalculator.calculate(it)}")
    }

    println("day 2")
    ProgramAlarm.readFileInput(File("src/main/resources/day2/restored_input.txt")).let {
        println("part 1 result: ${ProgramAlarm.part1(it.toMutableList())}")
        println("part 2 result: ${ProgramAlarm.part2(it.toMutableList())}")
    }
}