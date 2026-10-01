cmake_minimum_required(VERSION 3.22.1)
project("native-lib")

add_library(native-lib SHARED secrets.cpp)

find_library(log-lib log)
target_link_libraries(native-lib ${log-lib})

if(NOT DEFINED BASE_URL)
    set(BASE_URL "")
endif()
target_compile_definitions(native-lib PRIVATE BASE_URL="${BASE_URL}")
