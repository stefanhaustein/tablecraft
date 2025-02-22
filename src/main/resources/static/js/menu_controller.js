let menuSelectElement = document.getElementById("menuSelect")

menuSelectElement.addEventListener("change", () => {
    switch (menuSelectElement.value) {
        case "Load": uploadSpreadsheet(); break;
        case "Save": downloadSpreadsheet(); break;
    }
    menuSelectElement.value = "Menu"
})


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
            let formData = new FormData();
            formData.append("file", uploadFileInputElement.files[0]);
            fetch('/upload', {
                method: "POST",
                body: formData
            }).then(() => {
                window.location.reload()
            }, () => { alert("Upload Failed")})
        })
    }
    uploadDialog.showModal()
}
