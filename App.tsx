import React, { useEffect } from 'react';
import { StyleSheet, View } from 'react-native';
import { ScanDevices } from './src/screens/bleConnection/scanDevices';
import { pubsub } from './src/pubSub';

function App(): JSX.Element {
  useEffect(() => {
    pubsub.subscribe({ topics: [] }).subscribe();
  }, []);

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
    backgroundColor: '#BDBAB9',
  },
});

export default App;
