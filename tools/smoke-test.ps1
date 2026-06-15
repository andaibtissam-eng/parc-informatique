$ErrorActionPreference = "Stop"

trap {
    Write-Host "ERREUR: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response -and $_.Exception.Response.GetResponseStream()) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $body = $reader.ReadToEnd()
        if ($body) {
            Write-Host $body -ForegroundColor Yellow
        }
    }
    break
}

function Convert-Body {
    param([Parameter(Mandatory = $true)] $Value)
    return $Value | ConvertTo-Json -Depth 8
}

function Invoke-ApiPost {
    param(
        [Parameter(Mandatory = $true)] [string] $Url,
        [Parameter(Mandatory = $true)] $Body,
        [hashtable] $Headers = @{}
    )

    return Invoke-RestMethod -Uri $Url -Method Post -ContentType "application/json" -Body (Convert-Body $Body) -Headers $Headers
}

function Invoke-ApiGet {
    param(
        [Parameter(Mandatory = $true)] [string] $Url,
        [hashtable] $Headers = @{}
    )

    return Invoke-RestMethod -Uri $Url -Method Get -Headers $Headers
}

function Invoke-ApiPatch {
    param(
        [Parameter(Mandatory = $true)] [string] $Url,
        [hashtable] $Headers = @{},
        $Body = $null
    )

    if ($null -eq $Body) {
        return Invoke-RestMethod -Uri $Url -Method Patch -Headers $Headers
    }

    return Invoke-RestMethod -Uri $Url -Method Patch -ContentType "application/json" -Body (Convert-Body $Body) -Headers $Headers
}

$baseUrl = "http://localhost:8080/api"
$stamp = Get-Date -Format "HHmmss"
$registeredEmployeeEmail = "registered.employee.$stamp@emsi.ma"
$registeredTechnicianEmail = "registered.tech.$stamp@emsi.ma"
$employeeEmail = "employee.smoke.$stamp@emsi.ma"
$technicianEmail = "tech.smoke.$stamp@emsi.ma"

$adminLogin = Invoke-ApiPost -Url "$baseUrl/auth/login" -Body @{
    email = "admin@emsi.ma"
    password = "Admin123*"
}
$adminHeaders = @{ Authorization = "Bearer $($adminLogin.data.accessToken)" }
Write-Host "Admin login OK"

$employeeRegistration = Invoke-ApiPost -Url "$baseUrl/auth/register" -Body @{
    firstName = "Registered"
    lastName = "Employee"
    email = $registeredEmployeeEmail
    phone = "0600000000"
    departmentCode = "DSI"
    role = "EMPLOYE"
    password = "Employee123*"
}
Write-Host "Employee registration OK"

$technicianRegistration = Invoke-ApiPost -Url "$baseUrl/auth/register" -Body @{
    firstName = "Smoke"
    lastName = "Tech"
    email = $registeredTechnicianEmail
    phone = "0600000001"
    departmentCode = "DSI"
    role = "TECHNICIEN"
    password = "Tech12345*"
}
Write-Host "Technician registration OK"

$references = Invoke-ApiGet -Url "$baseUrl/references" -Headers $adminHeaders
Write-Host "References loaded"

$employee = (Invoke-ApiPost -Url "$baseUrl/users" -Headers $adminHeaders -Body @{
    firstName = "Smoke"
    lastName = "Employee"
    email = $employeeEmail
    phone = "0600000010"
    jobTitle = "Charge support"
    avatarUrl = ""
    status = "ACTIVE"
    language = "FR"
    enabled = $true
    emailVerified = $true
    departmentId = $references.data.departments[0].id
    roles = @("EMPLOYE")
    password = "Employee123*"
}).data
Write-Host "Smoke employee created"

$technician = (Invoke-ApiPost -Url "$baseUrl/users" -Headers $adminHeaders -Body @{
    firstName = "Smoke"
    lastName = "Tech"
    email = $technicianEmail
    phone = "0600000011"
    jobTitle = "Technicien maintenance"
    avatarUrl = ""
    status = "ACTIVE"
    language = "FR"
    enabled = $true
    emailVerified = $true
    departmentId = $references.data.departments[0].id
    roles = @("TECHNICIEN")
    password = "Tech12345*"
}).data
Write-Host "Smoke technician created"

$equipment = Invoke-ApiPost -Url "$baseUrl/equipments" -Headers $adminHeaders -Body @{
    name = "ThinkPad Smoke $stamp"
    brand = "Lenovo"
    model = "T14 Gen 5"
    serialNumber = "SN-$stamp"
    assetTag = "AST-$stamp"
    imageUrl = $null
    documentUrl = $null
    operatingSystem = "Windows 11 Pro"
    memoryGb = 16
    storageGb = 512
    processor = "Intel Core Ultra 7"
    purchaseDate = "2026-05-01"
    warrantyEndDate = "2028-05-01"
    acquisitionCost = 14999.99
    status = "AVAILABLE"
    notes = "Smoke test asset"
    categoryId = $references.data.categories[0].id
    supplierId = $references.data.suppliers[0].id
    locationId = $references.data.locations[0].id
    departmentId = $references.data.departments[0].id
}
Write-Host "Equipment created"

$assignment = Invoke-ApiPost -Url "$baseUrl/assignments" -Headers $adminHeaders -Body @{
    equipmentId = $equipment.data.id
    beneficiaryId = $employee.id
    startDate = (Get-Date).ToString("yyyy-MM-dd")
    expectedReturnDate = (Get-Date).AddDays(14).ToString("yyyy-MM-dd")
    digitalSignature = "Smoke Signature"
    notes = "Smoke assignment"
}
Invoke-ApiPatch -Url "$baseUrl/assignments/$($assignment.data.id)/approve" -Headers $adminHeaders | Out-Null
Write-Host "Assignment approved"

$employeeLogin = Invoke-ApiPost -Url "$baseUrl/auth/login" -Body @{
    email = $employeeEmail
    password = "Employee123*"
}
Write-Host "Smoke employee login OK"
$employeeHeaders = @{ Authorization = "Bearer $($employeeLogin.data.accessToken)" }
$employeeAssignments = Invoke-ApiGet -Url "$baseUrl/assignments?size=10" -Headers $employeeHeaders
Write-Host "Employee assignments loaded"

$maintenance = Invoke-ApiPost -Url "$baseUrl/maintenances" -Headers $employeeHeaders -Body @{
    equipmentId = $equipment.data.id
    title = "Incident clavier"
    description = "Une touche ne repond plus correctement."
    priority = "MEDIUM"
    status = "OPEN"
    slaDeadline = $null
    cost = $null
    partsReplaced = ""
    rootCause = ""
    notes = "Demande employee smoke"
}
Write-Host "Maintenance created"

$technicianLogin = Invoke-ApiPost -Url "$baseUrl/auth/login" -Body @{
    email = $technicianEmail
    password = "Tech12345*"
}
Write-Host "Smoke technician login OK"
$technicianHeaders = @{ Authorization = "Bearer $($technicianLogin.data.accessToken)" }
$maintenanceBoard = Invoke-ApiGet -Url "$baseUrl/maintenances/board" -Headers $technicianHeaders
Write-Host "Maintenance board loaded"

[pscustomobject]@{
    employeeRegisteredStatus = $employeeRegistration.data.status
    technicianRegisteredStatus = $technicianRegistration.data.status
    equipmentCreated = $equipment.data.inventoryCode
    assignmentCreated = $assignment.data.id
    employeeAssignmentsVisible = $employeeAssignments.data.content.Count
    maintenanceCreated = $maintenance.data.reference
    maintenanceBoardCount = $maintenanceBoard.data.Count
} | ConvertTo-Json -Depth 5
