import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login.component';
import { MainLayoutComponent } from './layout/main-layout/main-layout.component';
import { authGuard } from './core/guards/auth.guard';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { UsersComponent } from './features/users/users.component';
import { DriversComponent } from './features/drivers/drivers.component';
import { CoursesComponent } from './features/courses/courses.component';
import { MaterialsComponent } from './features/materials/materials.component';
import { LearningComponent } from './features/learning/learning.component';
import { LearningCourseComponent } from './features/learning/learning-course/learning-course.component';
import { LearningMaterialViewerComponent } from './features/learning/learning-material-viewer/learning-material-viewer.component';
import { LearningEvaluationComponent } from './features/learning/learning-evaluations/learning-evaluations.component';
import { QuestionsComponent } from './features/questions/questions.component';
import { CertificatesComponent } from './features/certificates/certificates.component';
import { CommunicationComponent } from './features/communication/communication.component';
import { CicLibraryComponent } from './features/cic/cic-library/cic-library.component';
import { CicCategoryComponent } from './features/cic/cic-category/cic-category.component';
import { permissionGuard } from './core/guards/permission.guard';
import { CicAdminComponent } from './features/cic/cic-admin/cic-admin.component';
import { EvaluationsComponent } from './features/evaluations/evaluations.component';
import { OperationsHomeComponent } from './features/operations/operations-home.component';
import { OperationHistoryComponent } from './features/operations/operation-history/operation-history.component';
import { ClientsComponent } from './features/clients/clients.component';
import { WarehousesComponent } from './features/warehouses/warehouses.component';
import { CargoTypesComponent } from './features/cargo-types/cargo-types.component';
import { OperationDetailComponent } from './features/operations/operation-detail/operation-detail.component';
import { RequirementCatalogComponent } from './features/operations/requirement-catalog/requirement-catalog.component';
import { LocationsComponent } from './features/locations/locations.component';
import { SubclientsComponent } from './features/subclients/subclients.component';
import { SchedulingComponent } from './features/scheduling/scheduling.component';
import { ScheduleDayComponent } from './features/scheduling/schedule-day/schedule-day.component';
import { OperationImportComponent } from './features/operations/operation-import/operation-import.component';
import { StatusCatalogComponent } from './features/status-catalog/status-catalog.component';
import { VehiclesComponent } from './features/vehicles/vehicles.component';
import { RolesComponent } from './features/roles/roles.component';
import { AuditReportComponent } from './features/reports/audit-report.component';

export const routes: Routes = [
    {
        path: 'login',
        component: LoginComponent
    },
    {
        path: '',
        component: MainLayoutComponent,
        canActivate: [authGuard],
        children: [
        {
            path: 'dashboard',
            component: DashboardComponent,
            canActivate: [permissionGuard],
            data: { permissions: ['DASHBOARD.VIEW'] }
        },
        {
            path: 'users',
            component: UsersComponent,
            canActivate: [permissionGuard],
            data: { permissions: ['USERS.VIEW'] }
        },
        {
            path: 'roles',
            component: RolesComponent,
            canActivate: [permissionGuard],
            data: { permissions: ['ROLES.VIEW'] }
        },
        {   
            path: 'drivers',
            component: DriversComponent,
            canActivate: [permissionGuard],
            data: { permissions: ['DRIVERS.VIEW'] }
        },
        {
            path: 'courses',
            component: CoursesComponent,
            canActivate: [permissionGuard],
            data: { permissions: ['TRAINING.COURSES.VIEW'] }
        },
        {
            path: 'courses/:courseId/materials',
            component: MaterialsComponent,
            canActivate: [permissionGuard],
            data: { permissions: ['TRAINING.MATERIALS.VIEW'] }
        },
        {

            path: 'questions',
            component: QuestionsComponent,
            canActivate: [permissionGuard],
            data: { permissions: ['TRAINING.QUESTIONS.VIEW'] }
        },
        {
            path: 'learning',
            component: LearningComponent,
            canActivate: [permissionGuard],
            data: { permissions: ['TRAINING.MY_COURSES.VIEW'] }
        },
        {
            path: 'learning/courses/:courseId',
            component: LearningCourseComponent,
            canActivate: [permissionGuard],
            data: { allPermissions: ['TRAINING.MY_COURSES.VIEW', 'TRAINING.MATERIALS.VIEW', 'TRAINING.PROGRESS.VIEW', 'TRAINING.EVALUATIONS.VIEW'] }
        },
        {   

            path: 'learning/courses/:courseId/material/:materialId',
            component: LearningMaterialViewerComponent,
            canActivate: [permissionGuard],
            data: { allPermissions: ['TRAINING.MY_COURSES.VIEW', 'TRAINING.MATERIALS.VIEW', 'TRAINING.PROGRESS.VIEW'] }
        },
        {
            path: 'learning/courses/:courseId/evaluation',
            component: LearningEvaluationComponent,
            canActivate: [permissionGuard],
            data: { allPermissions: ['TRAINING.MY_COURSES.VIEW', 'TRAINING.EVALUATIONS.EDIT'] }
        },
        {
            path: 'certificates',
            component: CertificatesComponent,
            canActivate: [permissionGuard],
            data: { permissions: ['CERTIFICATES.VIEW'] }
        },
        {
            path: 'my-evaluations',
            component: EvaluationsComponent,
            canActivate: [permissionGuard],
            data: { permissions: ['TRAINING.EVALUATIONS.VIEW'] }
        },
        {
            path: 'communication',
            component: CommunicationComponent,
            canActivate: [permissionGuard],
            data: { permissions: ['COMMUNICATIONS.SEND'] }
        },
        {
            path: 'reports/audit',
            component: AuditReportComponent,
            canActivate: [permissionGuard],
            data: { permissions: ['AUDIT.VIEW'] }
        },
        {
            path: 'operations',
            component: OperationsHomeComponent,
            canActivate: [permissionGuard],
            data: { permissions: ['OPERATIONS.VIEW'] }
        },
        {
            path: 'operations/import',
            component: OperationImportComponent,
            canActivate: [permissionGuard],
            data: { permissions: ['OPERATIONS.IMPORT'] }
        },
        {
            path: 'operations/history',
            component: OperationHistoryComponent,
            canActivate: [permissionGuard],
            data: { permissions: ['HISTORY.VIEW'] }
        },
        {
            path: 'scheduling/day/:date',
            component: ScheduleDayComponent,
            canActivate: [permissionGuard],
            data: { permissions: ['SCHEDULING.VIEW'] }
        },
        {
            path: 'scheduling',
            component: SchedulingComponent,
            canActivate: [permissionGuard],
            data: { permissions: ['SCHEDULING.VIEW'] }
        },
        {
            path: 'vehicles', component: VehiclesComponent,
            canActivate: [permissionGuard],
            data: { permissions: ['FLEET.VIEW'] }
        },
        {
            path: 'status-catalog',
            component: StatusCatalogComponent,
            canActivate: [permissionGuard],
            data: { permissions: ['CONFIGURATION.STATUSES.VIEW'] }
        },
        {
            path: 'operations/:id',
            component: OperationDetailComponent,
            canActivate: [permissionGuard],
            data: { permissions: ['OPERATIONS.VIEW'] }
        },
        {
        path: 'requirement-catalog',
        component: RequirementCatalogComponent,
            canActivate: [permissionGuard],
            data: { permissions: ['REQUIREMENTS.VIEW'] }
        },
        {
            path: 'clients',
            component: ClientsComponent,
            canActivate: [permissionGuard],
            data: { permissions: ['CLIENTS.VIEW'] }
        },
        {
            path: 'subclients',
            component: SubclientsComponent,
            canActivate: [permissionGuard],
            data: { permissions: ['CLIENTS.VIEW'] }
        },
        {
            path: 'warehouses',
            component: WarehousesComponent,
            canActivate: [permissionGuard],
            data: { permissions: ['WAREHOUSES.VIEW'] }
        },
        {
            path: 'cargo-types',
            component: CargoTypesComponent,
            canActivate: [permissionGuard],
            data: { permissions: ['CONFIGURATION.CARGO_TYPES.VIEW'] }
        },
        {
            path: 'locations',
            component: LocationsComponent,
            canActivate: [permissionGuard],
            data: { permissions: ['LOCATIONS.VIEW'] }
        },
        {
            path: 'cic',
            component: CicCategoryComponent,
            canActivate: [permissionGuard],
            data: { permissions: ['CIC.CATEGORIES.VIEW'] }
        },
        {
            path: 'cic/category/:id',
            component: CicLibraryComponent,
            canActivate: [permissionGuard],
            data: { permissions: ['CIC.DOCUMENTS.VIEW'] }
        },
        {
            path: 'cic/admin',
            component: CicAdminComponent,
            canActivate: [permissionGuard],
            data: { permissions: ['CIC.CATEGORIES.CREATE', 'CIC.CATEGORIES.EDIT', 'CIC.CATEGORIES.DELETE', 'CIC.DOCUMENTS.CREATE', 'CIC.DOCUMENTS.EDIT'] }
        }]
    },
    {
        path: '**',
        redirectTo: 'dashboard',
    }
];