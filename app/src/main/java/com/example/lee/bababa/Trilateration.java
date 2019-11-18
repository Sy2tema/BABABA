package com.example.lee.bababa;

public class Trilateration {
    public static position trilaterationFunction(position position1, position position2, position position3) {
        //position1,2,3 is beacon
        //		 position1
        // 	   	 /		  \
        // position2----position3
        //position4 is result position(x,y)
        //position class has (x,y), distance
        //you must have position.java, KalmanFilter in package

        double xa = position1.x;
        double ya = position1.y;
        double xb = position2.x;
        double yb = position2.y;
        double xc = position3.x;
        double yc = position3.y;
        double ra = position1.distance;
        double rb = position2.distance;
        double rc = position3.distance;

        double S = (Math.pow(xc, 2.) - Math.pow(xb, 2.) + Math.pow(yc, 2.) - Math.pow(yb, 2.) + Math.pow(rb, 2.) - Math.pow(rc, 2.)) / 2.0;
        double T = (Math.pow(xa, 2.) - Math.pow(xb, 2.) + Math.pow(ya, 2.) - Math.pow(yb, 2.) + Math.pow(rb, 2.) - Math.pow(ra, 2.)) / 2.0;
        double y = ((T * (xb - xc)) - (S * (xb - xa))) / (((ya - yb) * (xb - xc)) - ((yc - yb) * (xb - xa)));
        double x = ((y * (ya - yb)) - T) / (xb - xa);

        position position4 = new position(x, y);
        //result(position4)'s distance is 0(initialized), we don't need position4's distance
        return position4;
/*
    double dA = a.distance, dB = b.distance, dC = c.distance;
    double W =  W = dA*dA - dB*dB - a.x*a.x - a.y*a.y + b.x*b.x + b.y*b.y;
    double Z = dB*dB - dC*dC - b.x*b.x - b.y*b.y + c.x*c.x + c.y*c.y;
    double x = (W*(c.y-b.y) - Z*(b.y-a.y)) / (2 * ((b.x-a.x)*(c.y-b.y) - (c.x-b.x)*(b.y-a.y)));
    double y = (W - 2*x*(b.x-a.x)) / (2*(b.y-a.y));
    //double y2 = (Z - 2*x*(c.x-b.x)) / (2*(c.y-b.y));
    //y = (y + y2) / 2;
    position position4 = new position(x,y);

        return position4;
    }*/
    }
}
/*
(CGPoint)getCoordinateWithBeaconA:(CGPoint)a beaconB:(CGPoint)b beaconC:(CGPoint)c distanceA:(CGFloat)dA distanceB:(CGFloat)dB distanceC:(CGFloat)dC {
    CGFloat W, Z, x, y, y2;
    W = dA*dA - dB*dB - a.x*a.x - a.y*a.y + b.x*b.x + b.y*b.y;
    Z = dB*dB - dC*dC - b.x*b.x - b.y*b.y + c.x*c.x + c.y*c.y;

    x = (W*(c.y-b.y) - Z*(b.y-a.y)) / (2 * ((b.x-a.x)*(c.y-b.y) - (c.x-b.x)*(b.y-a.y)));
    y = (W - 2*x*(b.x-a.x)) / (2*(b.y-a.y));
    //y2 is a second measure of y to mitigate errors
    y2 = (Z - 2*x*(c.x-b.x)) / (2*(c.y-b.y));

    y = (y + y2) / 2;
    return CGPointMake(x, y);
}
*/
