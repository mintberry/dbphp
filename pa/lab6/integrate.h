
/* 
 * integrate.h -- public interface to the integrate module
 */
#define public
#define private static

typedef struct integral{
    double value;
    int strips;
}integral;

/* calculate the integral */
integral integrate(double (*fn)(double x), double l_bound, double u_bound, double precision);


