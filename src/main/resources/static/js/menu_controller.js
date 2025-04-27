let menuSelectElement = document.getElementById("menuSelect")

menuSelectElement.addEventListener("change", () => {
    switch (menuSelectElement.value) {
        case "Clear All": clearAll(); break;
        case "Load File": uploadSpreadsheet(); break;
        case "Load Example": loadExample(); break;
        case "Save File": downloadSpreadsheet(); break;
    }
    menuSelectElement.value = "Menu"
})

function clearAll() {
    if (confirm("Delete all data and start from scratch?")) {
        fetch('/clearAll', {
            method: "POST",
        }).then(() => {
            window.location.reload()
        }, () => { alert("Clear All Failed")})
    }
}


let firstDownload = true
function downloadSpreadsheet() {
    let downloadDialog = document.getElementById("downloadDialog")

    if (firstDownload) {
        firstDownload = false

        let downloadFileNameInputElement = document.getElementById("downloadFileNameInput")
        let downloadLink = document.getElementById("downloadLink")
        let downloadButton = document.getElementById("downloadButton")
        let downloadCancelButton = document.getElementById("downloadCancel")

        downloadFileNameInputElement.addEventListener("input", () => {
            downloadLink.setAttribute("download", downloadFileNameInputElement.value + ".ts")
        })
        downloadCancelButton.addEventListener("click", () => {downloadDialog.close() })
        downloadButton.addEventListener("click", () => {downloadDialog.close() })
    }
    downloadDialog.showModal()
}

let firstUpload = true
function uploadSpreadsheet() {
    let uploadDialog = document.getElementById("uploadDialog")
    if (firstUpload) {
        firstUpload = false

        let uploadFileInputElement = document.getElementById("uploadFileInput")
        let uploadButton = document.getElementById("uploadButton")
        let uploadCancelButton = document.getElementById("uploadCancel")

        uploadCancelButton.addEventListener("click", () => {uploadDialog.close() })
        uploadButton.addEventListener("click", () => {
            if (confirm("Replace the current spreadsheet with the selected file?")) {
                let formData = new FormData();
                formData.append("file", uploadFileInputElement.files[0]);
                fetch('/upload', {
                    method: "POST",
                    body: formData
                }).then(() => {
                    window.location.reload()
                }, () => {
                    alert("Upload Failed")
                })
            }
        })
    }
    uploadDialog.showModal()
}



let firstExample = true
function loadExample() {
    let exampleDialog = document.getElementById("exampleDialog")
    if (firstExample) {
        firstExample = false

        let exampleSelectElement = document.getElementById("exampleSelect")
        let exampleButton = document.getElementById("exampleButton")
        let exampleCancelButton = document.getElementById("exampleCancel")

        exampleCancelButton.addEventListener("click", () => { exampleDialog.close() })
        exampleButton.addEventListener("click", () => {
            if (confirm("Replace the current spreadsheet with the example file '" + exampleSelectElement.value + "'?")) {
            fetch('/loadExample', {
                method: "POST",
                body: exampleSelectElement.value
            }).then(() => {
                window.location.reload()
            }, () => { alert("Loading Example Failed")})
            }
        })
    }
    exampleDialog.showModal()
}
