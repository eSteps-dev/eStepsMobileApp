import React from 'react';
import { NativeModules, View, Text, TouchableOpacity, StyleSheet } from 'react-native';

interface CalibrationProps {
  device: string;
}
const ResetCalibrationData = ({ device }: CalibrationProps) => {
  const { ResetMacroDataA } = NativeModules;

  const handleRemoveCalibration = () => {
    ResetMacroDataA.resetCalibration(device);
  };

  return (
    <View style={styles.container}>
      <Text style={styles.text}>Device: {device}</Text>
      <View style={styles.buttonContainer}>
        <TouchableOpacity onPress={handleRemoveCalibration} style={styles.button}>
          <Text>{'Remove Calibration'}</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  button: {
    alignItems: 'center',
    backgroundColor: '#eee',
    flex: 1,
    justifyContent: 'center',
    padding: 10,
  },
  buttonContainer: {
    alignItems: 'stretch',
    flexDirection: 'row',
    marginTop: 15,
  },
  container: {
    flex: 1,
    justifyContent: 'center',
    paddingHorizontal: 20,
  },
  text: {
    textAlign: 'center',
  },
});

export default ResetCalibrationData;
