package net.cattaka.robotarm01.service;

interface IAdkService {
    boolean isConnected();
    boolean sendCommand(in byte cmd, in byte addr, in byte[] data);
    boolean connectDevice();
}
