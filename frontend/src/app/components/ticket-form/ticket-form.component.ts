import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { TicketService } from '../../services/ticket.service';
import { Ticket } from '../../models/ticket.model';

@Component({
  selector: 'app-ticket-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './ticket-form.component.html',
  styleUrls: ['./ticket-form.component.css']
})
export class TicketFormComponent {
  ticketForm: FormGroup;
  isSubmitting = false;
  submitSuccess = false;
  submitError = '';

  priorities = ['Low', 'Medium', 'High', 'Critical'];

  constructor(
    private fb: FormBuilder,
    private ticketService: TicketService
  ) {
    this.ticketForm = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(3)]],
      description: ['', [Validators.required, Validators.minLength(10)]],
      priority: ['Medium', Validators.required]
    });
  }

  onSubmit() {
    if (this.ticketForm.valid) {
      this.isSubmitting = true;
      this.submitError = '';
      this.submitSuccess = false;

      const ticket: Ticket = {
        ...this.ticketForm.value,
        status: 'Open'
      };

      this.ticketService.createTicket(ticket).subscribe({
        next: (response) => {
          this.submitSuccess = true;
          this.isSubmitting = false;
          this.ticketForm.reset();
          this.ticketForm.patchValue({ priority: 'Medium' });
        },
        error: (error) => {
          this.submitError = 'Failed to submit ticket. Please try again.';
          this.isSubmitting = false;
          console.error('Error submitting ticket:', error);
        }
      });
    }
  }

  get title() { return this.ticketForm.get('title'); }
  get description() { return this.ticketForm.get('description'); }
  get priority() { return this.ticketForm.get('priority'); }
}
