import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import TempViewA from './tempConnection';
import AccelConnectionViewA from './accelConnection';
import GyroConnectionViewA from './gyroConnection';
import CalibrateSensor from '../calibrateSensor';
import LevelBattery from './infoSensor/batteryLevel';
import InfoDevice from './infoSensor/info';

interface HomeProps {
  device: string;
}
export const HomeConnectionBleA = ({ device }: HomeProps) => {
  return (
    <View style={styles.container}>
      <Text style={styles.textStyle}>Device: {device}</Text>
      <LevelBattery device={device} />
      <InfoDevice device={device} />
      <AccelConnectionViewA device={device} />
      <GyroConnectionViewA device={device} />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    backgroundColor: 'white',
    flex: 1,
    flexDirection: 'column',
    padding: 10,
    borderRadius: 10,
  },
  textStyle: {
    textAlign: 'center',
    fontSize: 20,
    fontWeight: 'bold',
    backgroundColor: '#F7C4B9',
  },
  TempSection: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
  },
});
