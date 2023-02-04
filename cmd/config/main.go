package main

import (
	"flag"
	"fmt"
	"github.com/salex-org/smart-home-observer/internal/config"
	"os"
)

var (
	inputArg  *string
	outputArg *string
)

func main() {
	parseArgsAndExitIfError()
	if len(flag.Args()) < 1 {
		printUsageAndExit()
	}
	switch flag.Arg(0) {
	//case "edit":
	//	edit()
	case "encrypt":
		process(config.Encrypt)
	case "decrypt":
		process(config.Decrypt)
	default:
		printUsageAndExit()
	}
}

func parseArgsAndExitIfError() {
	inputArg = flag.String("i", "", "")
	outputArg = flag.String("o", "", "")
	shouldExit := false
	flag.Parse()
	if len(*inputArg) == 0 {
		fmt.Println("Error: No input filename specified")
		shouldExit = true
	}
	if len(*outputArg) == 0 {
		fmt.Println("Error: No output filename specified")
		shouldExit = true
	}
	if shouldExit {
		os.Exit(1)
	}
}

func printUsageAndExit() {
	fmt.Println("Usage:")
	fmt.Println("\tcet -i <input-file> -o <output-file> <command>")
	//	fmt.Println("\tcet edit [-f] [-k]")
	fmt.Println("\nCommands:")
	fmt.Println("\tencrypt\t\tEncrypts the input file with the provided AES key")
	fmt.Println("\tdecrypt\t\tDecrypts the input file with the provided AES key")
	//	fmt.Println("\tedit\t\tOpens the file with the provided AES key for editing")
	fmt.Println("\nOptions:")
	fmt.Println("\t-i\tThe input file for encryption/decryption")
	fmt.Println("\t-o\tThe output file for encryption/decryption")
	//	fmt.Println("\t-f\tThe file to edit - mandatory to be defined when using edit command")
	os.Exit(1)
}

func process(processor func([]byte, []byte) ([]byte, error)) {
	key, keyErr := config.ReadKey()
	if keyErr != nil {
		fmt.Printf("\nError reading key: %v\n", keyErr)
		return
	}
	input, inputErr := os.ReadFile(*inputArg)
	if inputErr != nil {
		fmt.Printf("\nError reading input: %v\n", inputErr)
		return
	}
	output, processErr := processor(input, key)
	if processErr != nil {
		fmt.Printf("\nError processing: %v\n", processErr)
		return
	}
	outputErr := os.WriteFile(*outputArg, output, 0)
	if outputErr != nil {
		fmt.Printf("\nError writing output: %v\n", outputErr)
		return
	}
}

//func edit() {
//	app := tview.NewApplication()
//	configFileName := "observer-config.yaml"
//	root, _ := createTree(configFileName)
//	tree := tview.NewTreeView().SetRoot(root).SetCurrentNode(root)
//	tree.SetBorder(true)
//	if err := app.SetRoot(tree, true).Run(); err != nil {
//		panic(err)
//	}
//}
//
//func createTree(filename string) (*tview.TreeNode, error) {
//	content, err := os.ReadFile(filename)
//	if err != nil {
//		fmt.Println(err)
//		return nil, err
//	}
//	dynamic := make(map[interface{}]interface{})
//	yaml.Unmarshal(content, &dynamic)
//	root := tview.NewTreeNode(filename)
//	root.SetColor(tcell.ColorDarkGray)
//	addChildren(root, dynamic)
//	return root, nil
//}
//
//func addChildren(parent *tview.TreeNode, data map[interface{}]interface{}) {
//	for key, value := range data {
//		child := tview.NewTreeNode(key.(string) + ":")
//		parent.AddChild(child)
//		subData, ok := value.(map[interface{}]interface{})
//		if ok {
//			addChildren(child, subData)
//		} else {
//			isEncrypted := strings.HasPrefix(value.(string), "{cipher}")
//			child.SetText(child.GetText() + " " + value.(string))
//			if isEncrypted {
//				child.SetText(child.GetText() + " \U0001F512")
//				child.SetColor(tcell.ColorGreen)
//			} else {
//				child.SetColor(tcell.ColorOrange)
//			}
//		}
//	}
//}
