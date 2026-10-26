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
            path: 'communication',
            component: CommunicationComponent
        }]
    },
    {
        path: '**',
        redirectTo: 'dashboard',
    }
];