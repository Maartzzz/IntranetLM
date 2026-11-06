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
import { adminGuard } from './core/guards/admin.guard';
import { CicAdminComponent } from './features/cic/cic-admin/cic-admin.component';
import { EvaluationsComponent } from './features/evaluations/evaluations.component';
import { OperationsHomeComponent } from './features/operations/operations-home.component';
import { ClientsComponent } from './features/clients/clients.component';
import { WarehousesComponent } from './features/warehouses/warehouses.component';
import { CargoTypesComponent } from './features/cargo-types/cargo-types.component';
import { OperationDetailComponent } from './features/operations/operation-detail/operation-detail.component';
import { RequirementCatalogComponent } from './features/operations/requirement-catalog/requirement-catalog.component';
import { LocationsComponent } from './features/locations/locations.component';
import { SubclientsComponent } from './features/subclients/subclients.component';
import { SchedulingComponent } from './features/scheduling/scheduling.component';
import { ScheduleDayComponent } from './features/scheduling/schedule-day/schedule-day.component';

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
            component: DashboardComponent
        },
        {
            path: 'users',
            component: UsersComponent
        },
        {   
            path: 'drivers',
            component: DriversComponent
        },
        {
            path: 'courses',
            component: CoursesComponent
        },
        {
            path: 'courses/:courseId/materials',
            component: MaterialsComponent
        },
        {

            path: 'questions',
            component: QuestionsComponent

        },
        {
            path: 'learning',
            component: LearningComponent
        },
        {
            path: 'learning/courses/:courseId',
            component: LearningCourseComponent
        },
        {   
            
            path: 'learning/courses/:courseId/material/:materialId',
            component: LearningMaterialViewerComponent

        },
        {
            path: 'learning/courses/:courseId/evaluation',
            component: LearningEvaluationComponent
        },
        {
            path: 'certificates',
            component: CertificatesComponent
        },
        {
            path: 'my-evaluations',
            component: EvaluationsComponent
        },
        {
            path: 'communication',
            component: CommunicationComponent
        },
        {
            path: 'operations',
            component: OperationsHomeComponent
        },
        {
            path: 'scheduling/day/:date',
            component: ScheduleDayComponent
        },
        {
            path: 'scheduling',
            component: SchedulingComponent
        },
        {
            path: 'operations/:id',
            component: OperationDetailComponent
        },
        {
        path: 'requirement-catalog',
        component: RequirementCatalogComponent
        },
        {
            path: 'clients',
            component: ClientsComponent
        },
        {
            path: 'subclients',
            component: SubclientsComponent
        },
        {
            path: 'warehouses',
            component: WarehousesComponent
        },
        {
            path: 'cargo-types',
            component: CargoTypesComponent
        },
        {
            path: 'locations',
            component: LocationsComponent
        },
        {
            path: 'cic',
            component: CicCategoryComponent
        },
        {
            path: 'cic/category/:id',
            component: CicLibraryComponent
        },
        {
            path: 'cic/admin',
            component: CicAdminComponent,
            canActivate: [adminGuard]
        }]
    },
    {
        path: '**',
        redirectTo: 'dashboard',
    }
];