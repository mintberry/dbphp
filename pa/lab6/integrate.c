/*
 * integrate.c
 * implementation of integrate
 * author: xiaochen qi
 */
#include "integrate.h"

#include<stdio.h>
#include<stdlib.h>
#include<math.h>

double approximation(double (*fn)(double x), double l_bound, double u_bound, int strips){
    double interval = u_bound - l_bound;
    double width = interval / strips, l_side, r_side;
    double ret = 0.0;
    int i = 0;
    for(;i != strips;++i){
        l_side = fn(l_bound + i * width);
        r_side = fn(l_bound + (i + 1) * width);
        ret += ((l_side + r_side) * width / 2);
    }
    return ret;
}

integral integrate(double (*fn)(double x), double l_bound, double u_bound, double precision){
    integral ret;
    double result = 0.0, last_result = 0.0;
    int strips = 1;
    ret.value = 0.0;
    ret.strips = strips;
    if(l_bound < u_bound){
        while(fabs((result = approximation(fn, l_bound, u_bound, strips)) - last_result) > precision || 1 == strips){
            last_result = result;
            strips *= 2;
        }
        ret.value = result;
        ret.strips = strips;
    } else if (l_bound > u_bound) {
        printf("interval error");
    }
    return ret;
}
