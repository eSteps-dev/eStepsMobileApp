import React, { useEffect, useState } from 'react';
import { NativeModules, View, Text, StyleSheet, NativeEventEmitter } from 'react-native';
import { pubsub } from '../../../pubSub';

interface infoProps {
  device: string;
}
const StepsA = ({ device }: infoProps) => {
  const { StepDetectionA } = NativeModules;
  const eventEmitter = new NativeEventEmitter(StepDetectionA);

  const [steps, setSteps] = useState<number>(0);

  useEffect(() => {
    const subscription = eventEmitter.addListener('steps', (steps: any) => {
      const { step } = steps;
      const timestamp =  new Date().getTime();
      if (step) {
        let stepsNumber = steps
        stepsNumber ++;
        setSteps(stepsNumber);
        
        const topics = `esteps/steps`;
        const sendMqttMessage = async() => {
          await pubsub.publish({ 
            topics,
            message : {stepsNumber, timestamp}
          });
        };
        sendMqttMessage();
      }
    });

    return () => {
      subscription.remove();
    };
  }, []);
  useEffect(() => {
    StepDetectionA.bindStepsService(device);
    return () => {
        StepDetectionA.unbindStepsService();
    };
  }, []);

  return (
    <View style={styles.container}>
      <Text style={styles.text}>Steps: {steps}</Text>   
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    paddingHorizontal: 20,
  },
  text: {
    textAlign: 'center',
  },
});

export default StepsA;
