JAVAFX_SDK = libs/javafx-sdk-23.0.1
SRC_DIR = src
BIN_DIR = bin
PACKAGE = taskChainPlanner
SRC_FILES = $(wildcard $(SRC_DIR)/*.java)
MAIN_CLASS = $(PACKAGE).Main
JAVAFX_MODULES = javafx.controls,javafx.fxml
JAVAC_FLAGS = --module-path $(JAVAFX_SDK)/lib --add-modules $(JAVAFX_MODULES) -d $(BIN_DIR)
JAVA_FLAGS = --module-path $(JAVAFX_SDK)/lib --add-modules $(JAVAFX_MODULES) -cp $(BIN_DIR)

all: $(BIN_DIR)/$(PACKAGE)/$(MAIN_CLASS).class

$(BIN_DIR)/$(PACKAGE)/$(MAIN_CLASS).class: $(SRC_FILES)
	@mkdir -p $(BIN_DIR)
	javac $(JAVAC_FLAGS) -sourcepath $(SRC_DIR) $^

run: all
	java $(JAVA_FLAGS) $(MAIN_CLASS)

clean:
	rm -rf $(BIN_DIR)/*
