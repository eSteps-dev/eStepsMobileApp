import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import TempViewA from './tempConnection';
import AccelConnectionViewA from './accelConnection';
import GyroConnectionViewA from './gyroConnection';

interface HomeProps {
  device: string;
}
export const HomeConnectionBleA = ({ device }: HomeProps) => {
  return (
    <View style={styles.container}>
      <Text style={styles.textStyle}>Device: {device}</Text>
      <TempViewA device={device} />
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
    justifyContent: 'space-between',
    padding: 10,
  },
  textStyle: {
    textAlign: 'center',
    fontSize: 20,
    fontWeight: 'bold',
    backgroundColor: '#F7C4B9',
  },
});
