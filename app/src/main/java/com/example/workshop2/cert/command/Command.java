package com.example.workshop2.cert.command;

public interface Command {
    void execute();
    void undo();
}