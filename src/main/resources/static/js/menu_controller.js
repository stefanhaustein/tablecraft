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
        let downloadFileNameInputElement = document.getElementById("downloadFileNameInput")
        let downloadLink = document.getElementById("downloadLink")
        let downloadButton = document.getElementById("downloadButton")
        let downloadCancelButton = document.getElementById("downloadCancel")

        downloadFileNameInputElement.addEventListener("input", () => {
            downloadLink.setAttribute("download", downloadFileNameInputElement.value)
        })
        downloadCancelButton.addEventListener("click", () => {downloadDialog.close() })
        downloadButton.addEventListener("click", () => {downloadDialog.close() })

        firstDownload = false
    }

    downloadDialog.showModal()
}


function uploadSpreadsheet() {
    let uploadDialog = document.getElementById("uploadDialog")

}