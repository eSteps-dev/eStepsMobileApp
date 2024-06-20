import React from 'react';
import { StyleSheet, View } from 'react-native';
import { ScanDevices } from 'screens/bleConnection/scanDevices';

function App(): JSX.Element {
  return (
    <View style={styles.container}>
      <ScanDevices />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    flexDirection: 'column',
  },
});

export default App;
