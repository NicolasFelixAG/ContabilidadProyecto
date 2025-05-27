# Makefile para compilar y ejecutar el proyecto Java

SRC_DIR=src
BUILD_DIR=build
LIB_DIR=lib
CLASSPATH=$(LIB_DIR)/flatlaf-3.6.jar:$(LIB_DIR)/junit-platform-console-standalone-1.5.2.jar
MAIN_CLASS=almacen.Main

JAVA_FILES=$(shell find $(SRC_DIR) -name "*.java")

.PHONY: all run clean

all:
	mkdir -p $(BUILD_DIR)
	javac -encoding utf-8 -d $(BUILD_DIR) -cp "$(CLASSPATH)" $(JAVA_FILES)

run: all
	java -cp "$(BUILD_DIR):$(CLASSPATH)" $(MAIN_CLASS)

clean:
	rm -rf $(BUILD_DIR)
