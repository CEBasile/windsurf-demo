import { Routes } from '@angular/router';
import { TicketFormComponent } from './components/ticket-form/ticket-form.component';
import { TicketListComponent } from './components/ticket-list/ticket-list.component';

export const routes: Routes = [
  { path: '', redirectTo: '/submit', pathMatch: 'full' },
  { path: 'submit', component: TicketFormComponent },
  { path: 'tickets', component: TicketListComponent }
];
