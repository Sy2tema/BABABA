package com.example.lee.bababa;

class KalmanFilter {
        private double Q = 0.00001;
        private double R = 0.001;
        private double X = 0, P = 1, K;

        //ù��°���� �Է¹޾� �ʱ�ȭ �Ѵ�. ���������� ����ؼ� ���簪�� �����ؾ� �ϹǷ� �ݵ�� �ϳ��̻��� ���� �ʿ��ϹǷ�~
        KalmanFilter(double initValue) {
            X = initValue;
        }

        //���������� �������� ����Ѵ�
        private void measurementUpdate(){
            K = (P + Q) / (P + Q + R);
            P = R * (P + Q) / (R + P + Q);
        }

        //���簪�� �޾� ���� ������ �����ϰ� ��ȯ�Ѵ�
        public double update(double measurement){
            measurementUpdate();
            X = X + (measurement - X) * K;

            return X;
        }
    }