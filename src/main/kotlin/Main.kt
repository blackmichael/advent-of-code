package main

import day1.FuelCalculator
import day2.ProgramAlarm
import main.day3.CrossedWires
import main.day4.SecureContainer
import main.day5.SunnyWithAChanceOfAstroids
import main.day6.UniversalOrbitMap
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

    println("day 3")
    CrossedWires.readFileInput(File("src/main/resources/day3/input.txt")).let {
        // simple_test.txt = 1, 1, 12, 12
        // test1.txt = 50, 50, 350, 200
        // test2.txt = 50, 50, 350, 200
        // input.txt = 8000, 15000, 15000, 25000

        // println("part 1 result: ${CrossedWires.part1(it, 8000, 15000, 15000, 25000)}")
        // println("part 2 result: ${CrossedWires.part2(it, 8000, 15000, 15000, 25000)}")
    }

    println("day 4")
    println("part 1 result: ${SecureContainer.countPart1Passwords(153517, 630395)}")
    println("part 2 result: ${SecureContainer.countPart2Passwords(153517, 630395)}")

    println("day 5")
    SunnyWithAChanceOfAstroids.readFileInput(File("src/main/resources/day5/input.txt")).let {
        println("part 1 result: ${SunnyWithAChanceOfAstroids.part1(it)}")
        println("part 2 result: ${SunnyWithAChanceOfAstroids.part2(it)}")
    }

    println("day 6")
    UniversalOrbitMap.readFileInput(File("src/main/resources/day6/input.txt")).let {
        println("part 1 result: ${UniversalOrbitMap.part1(it)}")
        println("part 2 result: ${UniversalOrbitMap.part2(it)}")
    }
}