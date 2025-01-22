// ICalculatorService.aidl
package dev.mohancm.serverapp;

// Declare any non-default types here with import statements

interface ICalculatorService {
    int add(int a, int b);
    int subtract(int a, int b);
}