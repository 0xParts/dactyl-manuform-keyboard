mirror ([1, 0, 0]) {
  difference () {
    union () {
      difference () {
        union () {
          translate ([0, -1.9, 4.475]) {
            cube ([28.2, 3.8, 14.95], center=true);
            translate ([0, 0.2, 0]) {
              cube ([30.9, 1.1, 14.95], center=true);
            }
          }
          translate ([-7.05, -12.8, 5]) {
            cube ([14.1, 18, 10], center=true);
          }
          translate ([7.05, -12.8, 5]) {
            cube ([14.1, 18, 10], center=true);
          }
          translate ([0, -50.8, 4]) {
            cube ([28.2, 58, 8], center=true);
          }
        }
      }
    }
    translate ([7, 0, 6]) {
      rotate ([90.0,0.0,0.0]) {
        union () {
          translate ([0, 0, 10]) {
            minkowski () {
              cylinder ($fn=100, h=10, r=0.95, center=true);
              cube ([7.34, 1.56, 10], center=true);
            }
          }
          translate ([0, 1.91, 10.7]) {
            cube ([12.3, 4.18, 18.6], center=true);
          }
          translate ([-5.62, -0.58, 10.7]) {
            cube ([0.6, 0.8, 18.6], center=true);
          }
          translate ([5.62, -0.58, 10.7]) {
            cube ([0.6, 0.8, 18.6], center=true);
          }
          translate ([0, 4.5, 20]) {
            cube ([2, 5, 5], center=true);
          }
        }
      }
    }
    translate ([-7, 0, 6]) {
      rotate ([90.0,0.0,0.0]) {
        union () {
          translate ([0, 0, 10]) {
            cylinder ($fn=100, h=20, r=3.2, center=true);
          }
          translate ([0, 0.85, 11.2]) {
            cube ([10.5, 8, 17.6], center=true);
          }
          translate ([0, 4.5, 20]) {
            cube ([2, 5, 5], center=true);
          }
        }
      }
    }
    translate ([0, -50.8, 4]) {
      difference () {
        cube ([24, 55, 10], center=true);
        union () {
          union () {
            translate ([5.7, 24.55, -1.5]) {
              cube ([3, 5.9, 5], center=true);
            }
            translate ([5.7, 23.525, 2.5]) {
              cylinder ($fn=100, h=3, r=1, center=true);
            }
          }
          mirror ([0, 1, 0]) {
            union () {
              translate ([5.7, 24.55, -1.5]) {
                cube ([3, 5.9, 5], center=true);
              }
              translate ([5.7, 23.525, 2.5]) {
                cylinder ($fn=100, h=3, r=1, center=true);
              }
            }
          }
          mirror ([1, 0, 0]) {
            union () {
              translate ([5.7, 24.55, -1.5]) {
                cube ([3, 5.9, 5], center=true);
              }
              translate ([5.7, 23.525, 2.5]) {
                cylinder ($fn=100, h=3, r=1, center=true);
              }
            }
          }
          rotate ([0.0,0.0,180.0]) {
            union () {
              translate ([5.7, 24.55, -1.5]) {
                cube ([3, 5.9, 5], center=true);
              }
              translate ([5.7, 23.525, 2.5]) {
                cylinder ($fn=100, h=3, r=1, center=true);
              }
            }
          }
        }
      }
    }
  }
}
