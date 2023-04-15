mirror ([1, 0, 0]) {
  union () {
    translate ([15, -114, 20]) {
      difference () {
        linear_extrude (height=40, scale=0.9, center=true){
          minkowski () {
            square ([94, 64], center=true);
            circle ($fn=100, r=3);
          }
        }
        translate ([0, 0, 5.235262876812019]) {
          rotate ([14.999999999999998,7.999999999999999,0.0]) {
            translate ([0, 0, 1000]) {
              linear_extrude (height=2000, scale=2, center=true){
                square ([5000, 3500], center=true);
              }
            }
          }
        }
        translate ([0, 0, -5]) {
          intersection () {
            linear_extrude (height=1000, center=true){
              projection (cut = false) {
                translate ([0, 0, 1000]) {
                  intersection () {
                    scale ([49/50, 34/35, 1]) {
                      linear_extrude (height=40, scale=0.9, center=true){
                        minkowski () {
                          square ([94, 64], center=true);
                          circle ($fn=100, r=3);
                        }
                      }
                    }
                    translate ([0, 0, 5.235262876812019]) {
                      rotate ([14.999999999999998,7.999999999999999,0.0]) {
                        translate ([0, 0, 1000]) {
                          linear_extrude (height=2000, scale=2, center=true){
                            square ([5000, 3500], center=true);
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
            translate ([0, 0, 5.235262876812019]) {
              rotate ([14.999999999999998,7.999999999999999,0.0]) {
                translate ([0, 0, 1000]) {
                  linear_extrude (height=2000, scale=2, center=true){
                    square ([5000, 3500], center=true);
                  }
                }
              }
            }
          }
        }
        union () {
          translate ([43.5, 28.5, -19.25]) {
            cylinder ($fn=100, h=1.5, r=5.25, center=true);
          }
          translate ([-43.5, 28.5, -19.25]) {
            cylinder ($fn=100, h=1.5, r=5.25, center=true);
          }
          translate ([43.5, -28.5, -19.25]) {
            cylinder ($fn=100, h=1.5, r=5.25, center=true);
          }
          translate ([-43.5, -28.5, -19.25]) {
            cylinder ($fn=100, h=1.5, r=5.25, center=true);
          }
        }
      }
    }
    union () {
      translate ([3.5, -89, 0]) {
        translate ([0, 0, 2.9]) {
          union () {
            translate ([0, 0, 2.9]) {
              rotate ([90.0,0.0,0.0]) {
                cylinder ($fn=100, h=20, r=4, center=true);
              }
            }
            cube ([8, 20, 5.8], center=true);
          }
        }
      }
      translate ([25, -89, 0]) {
        translate ([0, 0, 2.9]) {
          union () {
            translate ([0, 0, 2.9]) {
              rotate ([90.0,0.0,0.0]) {
                cylinder ($fn=100, h=20, r=4, center=true);
              }
            }
            cube ([8, 20, 5.8], center=true);
          }
        }
      }
    }
    union () {
      translate ([3.5, -68.77, 0]) {
        translate ([0, 0, 2.9]) {
          difference () {
            union () {
              translate ([0, 0, 2.9]) {
                rotate ([90.0,0.0,0.0]) {
                  cylinder ($fn=100, h=20.46, r=4, center=true);
                }
              }
              cube ([8, 20.46, 5.8], center=true);
            }
            translate ([0, 10.260000000000002, 3.65]) {
              rotate ([90.0,0.0,0.0]) {
                cylinder ($fn=100, h=20.46, r=2.7, center=true);
              }
            }
          }
        }
      }
      translate ([25, -64.325, 0]) {
        translate ([0, 0, 2.9]) {
          difference () {
            union () {
              translate ([0, 0, 2.9]) {
                rotate ([90.0,0.0,0.0]) {
                  cylinder ($fn=100, h=29.35, r=4, center=true);
                }
              }
              cube ([8, 29.35, 5.8], center=true);
            }
            translate ([0, 19.150000000000002, 3.65]) {
              rotate ([90.0,0.0,0.0]) {
                cylinder ($fn=100, h=29.35, r=2.7, center=true);
              }
            }
          }
        }
      }
    }
  }
}
