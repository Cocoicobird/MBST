package com.smelldetection.entity;

import java.text.DecimalFormat;
import java.util.Arrays;

/**
 * 实现矩阵的运算
 * @author Cocoicobird
 * @version 1.0
 * class Matrix：
 * - add(Matrix target), multiply(double target): Linear Operations(线性运算)
 * - multiply(Matrix target): Multiplication(乘法运算)
 * - transpose(Matrix target)(): Transposed(转置)
 * - print()
 */
public class Matrix {
    private int row;
    private int column;
    private double[][] value;
    private int num;

    public Matrix() { }

    public Matrix(int row, int column) {
        this.row = row;
        this.column = column;
        this.value = new double[row][column];
    }

    public Matrix(int row, int column, double[][] value) {
        this.row = row;
        this.column = column;
        this.value = value;
    }

    public int col() {
        return this.column;
    }

    public int row() {
        return this.row;
    }

    public void setValue(int row, int column, double value) {
        this.value[row][column] = value;
    }

    public double getValue(int row, int column) {
        return this.value[row][column];
    }

    public Matrix add(Matrix target) throws Exception {
        if (this.row() != target.row() || this.col() != target.col()) {
            throw new Exception("The two matrices must be identical in addition and subtraction! " +
                    "(加减法运算时两个矩阵必须是同型矩阵！)");
        } else {
            double[][] result = new double[this.row][this.column];
            for (int i = 0; i < this.row; i++) {
                for (int j = 0; j < this.column; j++) {
                    result[i][j] = this.value[i][j] + target.value[i][j];
                }
            }
            return new Matrix(this.row, this.column, result);
        }
    }

    public Matrix multiply(double d) {
        double[][] result = new double[this.row][this.column];
        for (int i = 0; i < this.row; i++) {
            for (int j = 0; j < this.column; j++) {
                result[i][j] = d * this.value[i][j];
            }
        }
        return new Matrix(this.row, this.column, result);
    }

    public Matrix multiply(Matrix target) throws Exception {
        if (this.column != target.row) {
            throw new Exception("The number of columns in the left matrix must equal to the number of rows in the right matrix! " +
                    "(乘法运算时左边矩阵的列数必须等于右边矩阵的行数!)");
        } else {
            double[][] result = new double[this.row][target.column];
            double c = 0;
            for (int i = 0; i < this.row; i++) {
                for (int j = 0; j < target.column; j++) {
                    //求C的元素值
                    for (int k = 0; k < this.column; k++) {
                        c += this.value[i][k] * target.value[k][j];
                    }
                    result[i][j] = c;
                    c = 0;
                }
            }
            return new Matrix(this.row, target.column, result);
        }
    }

    public Matrix transpose() {
        double[][] result = new double[this.column][this.row];
        for (int i = 0; i < this.row; i++) {
            for (int j = 0; j < this.column; j++) {
                result[j][i] = this.value[i][j];
            }
        }
        return new Matrix(this.column, this.row, result);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < column; j++)
                result.append(value[i][j]).append(" ");
            result.append("\n");
        }
        return result.toString();
    }
}
