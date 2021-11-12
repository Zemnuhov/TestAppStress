package com.example.teststressappkotlin.Model

class KalmanFilter(val Q: Double, val R:Double, val F:Double = 1.0, val H:Double = 1.0) {
    private var X0: Double? = null
    private var P0: Double? = null
    private var state: Double? = null
    private var covariance: Double? = null

    public fun setState(state: Double, covariance: Double){
        this.state = state
        this.covariance = covariance
    }

    public fun correct(value: Double){
        X0 = F* state!!;
        P0 = F* covariance!! *F + Q;

        //measurement update - correction
        val K = H* P0!! /(H*P0!!*H + R);
        state = X0!! + K*(value - H*X0!!);
        covariance = (1 - K*H)*P0!!;
    }
}
/*Применение...

    var fuelData = GetData();
    var filtered = new List<double>();

    var kalman = new KalmanFilterSimple1D(f: 1, h: 1, q: 2, r: 15); // задаем F, H, Q и R
    kalman.SetState(fuelData[0], 0.1); // Задаем начальные значение State и Covariance
    foreach(var d in fuelData)
    {
        kalman.Correct(d); // Применяем алгоритм

        filtered.Add(kalman.State); // Сохраняем текущее состояние
    }
*/