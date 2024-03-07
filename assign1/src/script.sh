#!/bin/zsh

n=30

mkdir -p results

echo "" > results/multi_cpp.txt
echo "" > results/line_multi_cpp.txt
echo "" > results/multi_java.txt
echo "" > results/line_multi_java.txt

for ((i = 600; i <= 3000; i += 400)); do

    echo "${i}x${i}"
    multi=""
    line_multi=""

    for ((j = 1; j <= n; j++)); do
        multi="${multi}1\n${i}\n"
        line_multi="${line_multi}2\n${i}\n"
    done

    multi="${multi}^C"
    line_multi="${line_multi}^C"

    echo "${i}" >> results/multi_cpp.txt
    echo "${i}" >> results/multi_java.txt

    echo "Multiplication - C++"
    echo $multi | ./matrixproduct | grep -E 'Time: | DCM' | sed -E 's/.*cols \? //' >> results/multi_cpp.txt
    echo "Multiplication - Java"
    echo $multi | java MatrixProduct 2> /dev/null | grep 'Time: ' | sed -E 's/.*cols \? //' >> results/multi_java.txt

    echo "${i}" >> results/line_multi_cpp.txt
    echo "${i}" >> results/line_multi_java.txt

    echo "Line Multiplication - C++"
    echo $line_multi | ./matrixproduct | grep -E 'Time: | DCM' | sed -E 's/.*cols \? //' >> results/line_multi_cpp.txt
    echo "Line Multiplication - Java"
    echo $line_multi | java MatrixProduct 2> /dev/null | grep 'Time: ' | sed -E 's/.*cols \? //' >> results/line_multi_java.txt

done

n=15

echo "" > results/block_multi_128.txt
echo "" > results/block_multi_256.txt
echo "" > results/block_multi_512.txt

for ((i = 4096; i <= 10240; i += 2048)); do

    echo "${i}x${i}"
    block_multi_128=""
    block_multi_256=""
    block_multi_512=""


    for ((j = 1; j <= n; j++)); do
        block_multi_128="${block_multi_128}3\n${i}\n128\n"
        block_multi_256="${block_multi_256}3\n${i}\n256\n"
        block_multi_512="${block_multi_512}3\n${i}\n512\n"
    done

    block_multi_128="${block_multi_128}^C"
    block_multi_256="${block_multi_256}^C"
    block_multi_512="${block_multi_512}^C"

    echo "${i}" >> results/block_multi_128.txt
    echo "${i}" >> results/block_multi_256.txt
    echo "${i}" >> results/block_multi_512.txt

    echo "Block Multiplication 128"
    echo $block_multi_128 | ./matrixproduct | grep -E 'Time: | DCM' | sed -E 's/.*cols \? //' >> results/block_multi_128.txt
    echo "Block Multiplication 256"
    echo $block_multi_256 | ./matrixproduct | grep -E 'Time: | DCM' | sed -E 's/.*cols \? //' >> results/block_multi_256.txt
    echo "Block Multiplication 512"
    echo $block_multi_512 | ./matrixproduct | grep -E 'Time: | DCM' | sed -E 's/.*cols \? //' >> results/block_multi_512.txt

done
